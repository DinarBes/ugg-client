package com.ugg.client.kit.profile.domain.exceptions

/** Firebase deletes an account only shortly after a sign-in. */
class RecentSignInRequiredException :
    Exception("Для удаления нужно войти заново: выйдите, войдите по номеру и повторите")
