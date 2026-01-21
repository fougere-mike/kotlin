/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.scenegraph

/**
 * Builder for custom component attributes and children.
 *
 * Use this builder inside a `component()` call to set custom attributes
 * and add child nodes.
 *
 * Example:
 * ```kotlin
 * component("MyCustomButton", id = "btn1") {
 *     attr("customField", "value")
 *     attr("iconSize", 48)
 *     attr("enabled", true)
 *     children {
 *         label(id = "inner", text = "Label")
 *     }
 * }
 * ```
 */
@SGNodeDsl
public class ComponentBuilder {
    internal val attributes = mutableMapOf<String, String>()
    internal val childBuilder = LayoutBuilder()

    /** Set a string attribute on the component */
    public fun attr(name: String, value: String) { attributes[name] = value }

    /** Set a numeric attribute on the component */
    public fun attr(name: String, value: Number) { attributes[name] = value.toString() }

    /** Set a boolean attribute on the component */
    public fun attr(name: String, value: Boolean) { attributes[name] = if (value) "true" else "false" }

    /** Set a color attribute on the component */
    public fun attr(name: String, value: Color) { attributes[name] = value.hex }

    /** Set a Vector2D attribute on the component */
    public fun attr(name: String, value: Vector2D) { attributes[name] = "[${value.x}, ${value.y}]" }

    /** Set a Vector4D attribute on the component */
    public fun attr(name: String, value: Vector4D) { attributes[name] = "[${value.x}, ${value.y}, ${value.width}, ${value.height}]" }

    /** Add child nodes to this component */
    public fun children(init: LayoutBuilder.() -> Unit) { childBuilder.init() }
}

/**
 * DSL builder for declaring SceneGraph node hierarchies.
 *
 * This builder provides type-safe methods for adding common SceneGraph nodes
 * to a component's layout. The compiler extracts the node declarations at
 * compile time to generate XML and typed accessors.
 *
 * Example:
 * ```kotlin
 * sceneLayout {
 *     layoutGroup(id = "main", layoutDirection = LayoutDirection.vert) {
 *         label(id = "title", text = "Hello", font = "font:LargeBoldSystemFont")
 *         rectangle(id = "divider", width = 400f, height = 2f, color = Color.White)
 *         buttonGroup(id = "actions") {
 *             button(id = "ok", text = "OK")
 *             button(id = "cancel", text = "Cancel")
 *         }
 *     }
 * }
 * ```
 */
@SGNodeDsl
public class LayoutBuilder {
    private val nodes = mutableListOf<NodeEntry>()
    private val _interfaceFields = mutableListOf<InterfaceFieldEntry>()

    /**
     * Returns the list of declared nodes.
     * @return Immutable copy of the node list.
     */
    internal fun build(): List<NodeEntry> = nodes.toList()

    /**
     * Returns the list of interface field declarations.
     * @return Immutable copy of the interface fields list.
     */
    internal fun buildInterfaceFields(): List<InterfaceFieldEntry> = _interfaceFields.toList()

    // =========================================================================
    // Interface Fields
    // =========================================================================

    /**
     * Declares an interface field on this component.
     *
     * Interface fields can either:
     * 1. Alias a child node's field (making it observable on m.top)
     * 2. Be a standalone field with no alias (for component state/communication)
     *
     * Example:
     * ```kotlin
     * sceneLayout {
     *     // Simple alias to child field
     *     interfaceField("buttonSelected", alias = "incrementButton.buttonSelected")
     *
     *     // Standalone field with onChange (no alias)
     *     interfaceField(
     *         "counter",
     *         type = InterfaceFieldType.INTEGER,
     *         onChange = "onCounterChanged"
     *     )
     *
     *     // Aliased field with type and onChange
     *     interfaceField(
     *         "rowItemSelected",
     *         alias = "gridScreen.rowItemSelected",
     *         type = InterfaceFieldType.INT_ARRAY,
     *         alwaysNotify = true,
     *         onChange = "onRowItemSelected"
     *     )
     *
     *     // With default value
     *     interfaceField(
     *         "title",
     *         alias = "titleLabel.text",
     *         type = InterfaceFieldType.STRING,
     *         value = "Default Title"
     *     )
     *
     *     button(id = "incrementButton", text = "Click")
     * }
     * ```
     *
     * Then observe on `top` instead of the button:
     * ```kotlin
     * top.observeFieldScoped("buttonSelected", brsName(::onButtonPressed))
     * ```
     *
     * Generated XML:
     * ```xml
     * <interface>
     *     <field id="buttonSelected" alias="incrementButton.buttonSelected" />
     *     <field id="counter" type="integer" onChange="onCounterChanged" />
     *     <field id="rowItemSelected" alias="gridScreen.rowItemSelected" alwaysNotify="true" onChange="onRowItemSelected" />
     *     <field id="title" alias="titleLabel.text" value="Default Title" />
     * </interface>
     * ```
     *
     * @param name The field name exposed on this component's interface
     * @param alias The path to the child node's field (e.g., "buttonId.buttonSelected"), or null for standalone fields
     * @param type The field type (default: NODE)
     * @param value The default value for the field (optional)
     * @param onChange The callback function name to invoke when the field changes (optional)
     * @param alwaysNotify Whether to notify observers even when the value is unchanged (default: false)
     */
    public fun interfaceField(
        name: String,
        alias: String? = null,
        type: InterfaceFieldType = InterfaceFieldType.NODE,
        value: String? = null,
        onChange: String? = null,
        alwaysNotify: Boolean = false
    ) {
        _interfaceFields.add(InterfaceFieldEntry(name, alias, type, value, onChange, alwaysNotify))
    }

    // =========================================================================
    // Container Nodes
    // =========================================================================

    /**
     * Adds a Group node - basic container for organizing child nodes.
     *
     * Group nodes provide a way to organize multiple nodes together without
     * affecting their visual appearance or layout.
     *
     * @param id Unique identifier for this node.
     * @param translation Position relative to parent.
     * @param rotation Rotation in degrees.
     * @param scale Scale factors.
     * @param opacity Opacity from 0.0 to 1.0.
     * @param visible Whether this node is visible.
     * @param init Lambda to add child nodes.
     */
    public fun group(
        id: String,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null,
        init: LayoutBuilder.() -> Unit = {}
    ) {
        val childBuilder = LayoutBuilder()
        childBuilder.init()
        nodes.add(
            GroupEntry(
                id = id,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass,
                children = childBuilder.build()
            )
        )
    }

    /**
     * Adds a LayoutGroup node - arranges children in a row or column.
     *
     * LayoutGroup automatically positions its children either horizontally
     * or vertically with configurable spacing and alignment.
     *
     * @param id Unique identifier for this node.
     * @param layoutDirection Direction to arrange children (horiz or vert).
     * @param horizAlignment Horizontal alignment (when layoutDirection is vert).
     * @param vertAlignment Vertical alignment (when layoutDirection is horiz).
     * @param itemSpacings Spacing between children in pixels.
     * @param translation Position relative to parent.
     * @param init Lambda to add child nodes.
     */
    public fun layoutGroup(
        id: String,
        layoutDirection: LayoutDirection = LayoutDirection.horiz,
        horizAlignment: HorizAlignment? = null,
        vertAlignment: VertAlignment? = null,
        itemSpacings: List<Int>? = null,
        addItemSpacingAfterChild: Boolean? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null,
        init: LayoutBuilder.() -> Unit = {}
    ) {
        val childBuilder = LayoutBuilder()
        childBuilder.init()
        nodes.add(
            LayoutGroupEntry(
                id = id,
                layoutDirection = layoutDirection,
                horizAlignment = horizAlignment,
                vertAlignment = vertAlignment,
                itemSpacings = itemSpacings,
                addItemSpacingAfterChild = addItemSpacingAfterChild,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass,
                children = childBuilder.build()
            )
        )
    }

    // =========================================================================
    // Renderable Nodes
    // =========================================================================

    /**
     * Adds a Label node - displays text.
     *
     * @param id Unique identifier for this node.
     * @param text Text to display.
     * @param font Font specification (e.g., "font:MediumBoldSystemFont").
     * @param color Text color.
     * @param horizAlign Horizontal text alignment.
     * @param vertAlign Vertical text alignment.
     * @param width Width constraint.
     * @param height Height constraint.
     * @param translation Position relative to parent.
     */
    public fun label(
        id: String,
        text: String? = null,
        font: String? = null,
        color: Color? = null,
        horizAlign: HorizAlign? = null,
        vertAlign: VertAlign? = null,
        width: Float? = null,
        height: Float? = null,
        maxWidth: Float? = null,
        maxLines: Int? = null,
        wrap: Boolean? = null,
        truncateOnDelimiter: String? = null,
        lineSpacing: Float? = null,
        displayPartialLines: Boolean? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            LabelEntry(
                id = id,
                text = text,
                font = font,
                color = color,
                horizAlign = horizAlign,
                vertAlign = vertAlign,
                width = width,
                height = height,
                maxWidth = maxWidth,
                maxLines = maxLines,
                wrap = wrap,
                truncateOnDelimiter = truncateOnDelimiter,
                lineSpacing = lineSpacing,
                displayPartialLines = displayPartialLines,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    /**
     * Adds a Poster node - displays an image.
     *
     * @param id Unique identifier for this node.
     * @param uri Image URI (local or remote).
     * @param width Display width.
     * @param height Display height.
     * @param translation Position relative to parent.
     */
    public fun poster(
        id: String,
        uri: String? = null,
        width: Float? = null,
        height: Float? = null,
        loadingBitmapStyle: LoadingBitmapStyle? = null,
        loadingBitmapUri: String? = null,
        failedBitmapUri: String? = null,
        blendingMode: BlendingMode? = null,
        blendColor: Color? = null,
        loadSync: Boolean? = null,
        maskUri: String? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            PosterEntry(
                id = id,
                uri = uri,
                width = width,
                height = height,
                loadingBitmapStyle = loadingBitmapStyle,
                loadingBitmapUri = loadingBitmapUri,
                failedBitmapUri = failedBitmapUri,
                blendingMode = blendingMode,
                blendColor = blendColor,
                loadSync = loadSync,
                maskUri = maskUri,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    /**
     * Adds a Rectangle node - displays a colored rectangle.
     *
     * @param id Unique identifier for this node.
     * @param width Width in pixels.
     * @param height Height in pixels.
     * @param color Fill color.
     * @param translation Position relative to parent.
     */
    public fun rectangle(
        id: String,
        width: Float? = null,
        height: Float? = null,
        color: Color? = null,
        blendingMode: BlendingMode? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            RectangleEntry(
                id = id,
                width = width,
                height = height,
                color = color,
                blendingMode = blendingMode,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    // =========================================================================
    // Widget Nodes
    // =========================================================================

    /**
     * Adds a Button node - standard UI button.
     *
     * @param id Unique identifier for this node.
     * @param text Button label text.
     * @param textFont Font for button text.
     * @param textColor Text color when not focused.
     * @param focusedTextColor Text color when focused.
     * @param translation Position relative to parent.
     */
    public fun button(
        id: String,
        text: String? = null,
        textFont: String? = null,
        textColor: Color? = null,
        focusedTextColor: Color? = null,
        backgroundUri: String? = null,
        focusBitmapUri: String? = null,
        focusFootprint: String? = null,
        minWidth: Float? = null,
        maxWidth: Float? = null,
        height: Float? = null,
        iconUri: String? = null,
        focusedIconUri: String? = null,
        showFocusFootprint: Boolean? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            ButtonEntry(
                id = id,
                text = text,
                textFont = textFont,
                textColor = textColor,
                focusedTextColor = focusedTextColor,
                backgroundUri = backgroundUri,
                focusBitmapUri = focusBitmapUri,
                focusFootprint = focusFootprint,
                minWidth = minWidth,
                maxWidth = maxWidth,
                height = height,
                iconUri = iconUri,
                focusedIconUri = focusedIconUri,
                showFocusFootprint = showFocusFootprint,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    /**
     * Adds a ButtonGroup node - container that manages focus among buttons.
     *
     * @param id Unique identifier for this node.
     * @param layoutDirection Direction to arrange buttons.
     * @param itemSpacings Spacing between buttons.
     * @param translation Position relative to parent.
     * @param init Lambda to add Button children.
     */
    public fun buttonGroup(
        id: String,
        layoutDirection: LayoutDirection? = null,
        itemSpacings: List<Int>? = null,
        focusedButton: Int? = null,
        wrapDividerWidth: Int? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null,
        init: LayoutBuilder.() -> Unit = {}
    ) {
        val childBuilder = LayoutBuilder()
        childBuilder.init()
        nodes.add(
            ButtonGroupEntry(
                id = id,
                layoutDirection = layoutDirection,
                itemSpacings = itemSpacings,
                focusedButton = focusedButton,
                wrapDividerWidth = wrapDividerWidth,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass,
                children = childBuilder.build()
            )
        )
    }

    /**
     * Adds a TextEditBox node - text input field.
     *
     * @param id Unique identifier for this node.
     * @param text Current text value.
     * @param hint Placeholder text.
     * @param textColor Text color.
     * @param translation Position relative to parent.
     */
    public fun textEditBox(
        id: String,
        text: String? = null,
        hint: String? = null,
        textColor: Color? = null,
        hintTextColor: Color? = null,
        textFont: String? = null,
        maxTextLength: Int? = null,
        width: Float? = null,
        secureMode: Boolean? = null,
        cursorPosition: Int? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            TextEditBoxEntry(
                id = id,
                text = text,
                hint = hint,
                textColor = textColor,
                hintTextColor = hintTextColor,
                textFont = textFont,
                maxTextLength = maxTextLength,
                width = width,
                secureMode = secureMode,
                cursorPosition = cursorPosition,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    /**
     * Adds a Keyboard node - on-screen keyboard.
     *
     * @param id Unique identifier for this node.
     * @param text Current text entered.
     * @param keyboardType Type of keyboard layout.
     * @param translation Position relative to parent.
     */
    public fun keyboard(
        id: String,
        text: String? = null,
        keyboardType: KeyboardType? = null,
        lowercase: Boolean? = null,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null
    ) {
        nodes.add(
            KeyboardEntry(
                id = id,
                text = text,
                keyboardType = keyboardType,
                lowercase = lowercase,
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass
            )
        )
    }

    // =========================================================================
    // Custom Components
    // =========================================================================

    /**
     * Adds a custom user-defined component node.
     *
     * Use this to embed custom SceneGraph components that aren't built into the DSL.
     * Custom attributes can be set using `attr()` calls in the builder lambda.
     * Child nodes can be added using `children { }`.
     *
     * Example:
     * ```kotlin
     * component("MyCustomButton", id = "btn1") {
     *     attr("customField", "value")
     *     attr("iconSize", 48)
     * }
     *
     * component("CustomContainer", id = "container") {
     *     attr("padding", 20)
     *     children {
     *         label(id = "nested", text = "Inside container")
     *     }
     * }
     * ```
     *
     * @param componentType The name of the custom component type (e.g., "MyCustomButton").
     * @param id Unique identifier for this node.
     * @param translation Position relative to parent.
     * @param rotation Rotation in degrees.
     * @param scale Scale factors.
     * @param opacity Opacity from 0.0 to 1.0.
     * @param visible Whether this node is visible.
     * @param init Lambda to set custom attributes and add children.
     */
    public fun component(
        componentType: String,
        id: String,
        translation: Vector2D? = null,
        rotation: Float? = null,
        scale: Vector2D? = null,
        scaleRotateCenter: Vector2D? = null,
        opacity: Float? = null,
        visible: Boolean? = null,
        inheritParentOpacity: Boolean? = null,
        inheritParentTransform: Boolean? = null,
        clippingRect: Vector4D? = null,
        renderGroup: Boolean? = null,
        focusable: Boolean? = null,
        renderPass: Int? = null,
        init: ComponentBuilder.() -> Unit = {}
    ) {
        val builder = ComponentBuilder()
        builder.init()
        nodes.add(
            CustomComponentEntry(
                id = id,
                componentType = componentType,
                customAttributes = builder.attributes.toMap(),
                translation = translation,
                rotation = rotation,
                scale = scale,
                scaleRotateCenter = scaleRotateCenter,
                opacity = opacity,
                visible = visible,
                inheritParentOpacity = inheritParentOpacity,
                inheritParentTransform = inheritParentTransform,
                clippingRect = clippingRect,
                renderGroup = renderGroup,
                focusable = focusable,
                renderPass = renderPass,
                children = builder.childBuilder.build()
            )
        )
    }
}
