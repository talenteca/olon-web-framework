package olon
package http

import common.{Box, Empty}

object SpecContextHelpers {

  /** Wraps a spec in a context where `rules` are the Lift rules in effect.
    */
  def WithRules[T](rules: LiftRules)(body: => T) =
    LiftRulesMocker.devTestLiftRulesInstance.doWith(rules) {
      body
    }

  /** Wraps a spec in a context where `rules` are the Lift rules in effect,
    * `session` is the current Lift session, and `req`, if specified, is the
    * current request.
    */
  def WithLiftContext[T](
      rules: LiftRules,
      session: LiftSession,
      req: Box[Req] = Empty
  )(body: => T) = {
    LiftRulesMocker.devTestLiftRulesInstance.doWith(rules) {
      S.init(req, session) {
        body
      }
    }
  }
}
