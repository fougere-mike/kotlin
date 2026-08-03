// Expected: no diagnostic — any concrete class under a @BrsSceneGraphComponent base
// (here GroupComponent) is a component; inferred and explicit type arguments both pass
import kotlin.brs.GroupComponent
import kotlin.brs.createComponent

class Card : GroupComponent()

fun makeExplicit(): Card = createComponent<Card>()

fun makeInferred(): Card {
    val card: Card = createComponent()
    return card
}
