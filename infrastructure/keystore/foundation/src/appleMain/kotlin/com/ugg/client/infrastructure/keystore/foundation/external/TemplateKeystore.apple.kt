package com.ugg.client.infrastructure.keystore.foundation.external

import com.ugg.client.infrastructure.keystore.foundation.internal.asReadFailure
import com.ugg.client.infrastructure.keystore.foundation.internal.asWriteFailure
import com.ugg.client.infrastructure.keystore.foundation.internal.cfValue
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFAutorelease
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessGroup
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/**
 * Keychain-backed [Keystore] (`kSecClassGenericPassword`).
 *
 * Every entry is written with an explicit `kSecAttrAccessible`, in both the `SecItemAdd` query and
 * the `SecItemUpdate` attribute dictionary — setting it only on add would leave an existing entry
 * on its original protection class forever, since an update rewrites only what it is given.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class, BetaInteropApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class TemplateKeystore actual constructor(
    actual override val configuration: KeystoreConfiguration,
) : Keystore {

    private fun query(vararg pairs: Pair<CFStringRef?, CFTypeRef?>): CFMutableDictionaryRef? {
        val map = mapOf(*pairs).filter { it.key != null && it.value != null }

        return CFDictionaryCreateMutable(
            null, map.size.convert(), null, null
        ).apply {
            map.forEach { CFDictionaryAddValue(this, it.key, it.value) }
        }.apply {
            CFAutorelease(this)
        }
    }

    /** The attributes identifying a single entry — shared by lookup, update and delete. */
    private fun identity(key: String): Array<Pair<CFStringRef?, CFTypeRef?>> = arrayOf(
        kSecClass to kSecClassGenericPassword,
        kSecAttrAccount to CFBridgingRetain(key),
        kSecAttrService to configuration.service?.let { CFBridgingRetain(it) },
        kSecAttrAccessGroup to configuration.accessGroup?.let { CFBridgingRetain(it) },
    )

    actual override fun get(key: String): KeystoreReadResult {
        val query = query(
            *identity(key),
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne,
        )

        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)

            if (status != errSecSuccess) return status.asReadFailure()

            val data = CFBridgingRelease(result.value) as? NSData
                ?: return KeystoreReadResult.NotFound
            val value = NSString.create(data, NSUTF8StringEncoding)?.toString()
                ?: return KeystoreReadResult.NotFound

            return KeystoreReadResult.Found(value)
        }
    }

    actual override fun set(
        key: String,
        value: String?,
        accessibility: KeystoreAccessibility,
    ): KeystoreWriteResult {
        if (value == null) return delete(key)

        val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding)
            ?: return KeystoreWriteResult.Failure(
                KeystoreFailure.Unknown("could not encode the value for key $key")
            )

        // Existence probe without kSecReturnData: matching an entry does not need it decrypted,
        // so a locked device does not turn every write into TemporarilyUnavailable.
        val exists = SecItemCopyMatching(query(*identity(key), kSecMatchLimit to kSecMatchLimitOne), null)

        val status = when (exists) {
            errSecSuccess -> SecItemUpdate(
                query(*identity(key)),
                query(
                    kSecValueData to CFBridgingRetain(data),
                    kSecAttrAccessible to accessibility.cfValue,
                ),
            )

            errSecItemNotFound -> SecItemAdd(
                query(
                    *identity(key),
                    kSecValueData to CFBridgingRetain(data),
                    kSecAttrAccessible to accessibility.cfValue,
                ),
                null,
            )

            else -> return exists.asWriteFailure()
        }

        return if (status == errSecSuccess || status == errSecDuplicateItem) {
            KeystoreWriteResult.Stored
        } else {
            status.asWriteFailure()
        }
    }

    private fun delete(key: String): KeystoreWriteResult {
        val status = SecItemDelete(query(*identity(key)))

        return if (status == errSecSuccess || status == errSecItemNotFound) {
            KeystoreWriteResult.Stored
        } else {
            status.asWriteFailure()
        }
    }
}
