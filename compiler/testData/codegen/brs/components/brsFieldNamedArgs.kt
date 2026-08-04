import kotlin.brs.BrsComponent
import kotlin.brs.BrsField

// @BrsField's name/defaultValue/alias parameters:
// - defaultValue lands in the XML as the field's value attribute
// - name overrides the XML field id (interface-side rename only)
// - alias declares the field as an alias of a child node's field
@BrsComponent
class FieldArgsComponent {
    @BrsField(type = "string", defaultValue = "ready")
    val status: String = "ready"

    @BrsField(name = "buttonSelected", alias = "innerButton.buttonSelected")
    val selected: Boolean = false
}
