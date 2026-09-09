// Expected: BRS_CREATE_COMPONENT_HAS_INPUTS only — precedence pin: the has-inputs check runs
// BEFORE the invalid-type classification, so an input-bearing ABSTRACT class reports the inputs
// rule and not BRS_CREATE_COMPONENT_INVALID_TYPE (the inputs message names the more useful fix).
import kotlin.brs.GroupComponent
import kotlin.brs.SGStringField
import kotlin.brs.createComponent

abstract class BaseScreen(@SGStringField val airingId: String) : GroupComponent()

class Host : GroupComponent() {
    fun open() {
        createComponent<<!BRS_CREATE_COMPONENT_HAS_INPUTS!>BaseScreen<!>>()
    }
}
