/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.brs.roku

import kotlin.brs.BrsCreateObject
import kotlin.brs.BrsName
import kotlin.brs.Dynamic

// =============================================================================
// SceneGraph Interface Mappings
// =============================================================================

/**
 * Maps to BrightScript's ifSGScreen interface.
 * Provides SceneGraph screen creation and management.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsgscreen.md">ifSGScreen</a>
 */
public external interface ISGScreen {
    /**
     * Creates an instance of a SceneGraph scene component.
     *
     * @param sceneType The name of a component that extends the Scene node class.
     * @return The created Scene node.
     */
    public fun createScene(sceneType: String): RoSGNode

    /**
     * Returns the Scene node created with createScene().
     *
     * @return The Scene node, or null if no scene has been created.
     */
    public fun getScene(): RoSGNode?

    /**
     * Returns the global node for sharing data across all components.
     *
     * @return The global node (m.global).
     */
    public fun getGlobalNode(): RoSGNode

    /**
     * Displays the SceneGraph screen.
     */
    public fun show()

    /**
     * Closes the SceneGraph screen and releases associated resources.
     */
    public fun close()
}

/**
 * Maps to BrightScript's ifSGNodeField interface.
 * Provides field manipulation for SceneGraph nodes.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsgnodefield.md">ifSGNodeField</a>
 */
public external interface ISGNodeField {
    /**
     * Checks if a field exists on this node.
     *
     * @param fieldName The name of the field.
     * @return True if the field exists.
     */
    public fun hasField(fieldName: String): Boolean

    /**
     * Gets the value of a field.
     *
     * Reading an UNDECLARED field name returns null (invalid) — it does not
     * throw and does not create the field. Since a misnamed [setField] write
     * is silently dropped (never stored under the wrong name), a read-back
     * check cannot detect the typo either; see [setField].
     *
     * @param fieldName The name of the field.
     * @return The field value, or null if the field doesn't exist.
     */
    public fun getField(fieldName: String): Dynamic?

    /**
     * Gets all fields as an associative array.
     *
     * @return An associative array containing all field name-value pairs.
     */
    public fun getFields(): RoAssociativeArray

    /**
     * Adds a new field to the node.
     *
     * @param fieldName The name of the field.
     * @param type The BrightScript type (e.g., "string", "integer", "node").
     * @param alwaysNotify If true, observers are notified even when value doesn't change.
     * @return True if the field was added successfully.
     */
    public fun addField(fieldName: String, type: String, alwaysNotify: Boolean): Boolean

    /**
     * Adds multiple fields from an associative array.
     *
     * @param fields An associative array where keys are field names and values
     *               are associative arrays with "type" and optionally "value" keys.
     * @return True if all fields were added successfully.
     */
    public fun addFields(fields: RoAssociativeArray): Boolean

    /**
     * Gets the type of a field.
     *
     * @param fieldName The name of the field.
     * @return The field type as a string.
     */
    public fun getFieldType(fieldName: String): String

    /**
     * Gets the types of all fields.
     *
     * @return An associative array mapping field names to their types.
     */
    public fun getFieldTypes(): RoAssociativeArray

    /**
     * Sets the value of a field.
     *
     * Device-verified semantics (FieldSemantics suite, roku-test-app):
     * - Declared field, matching type: the value is set and observers fire
     *   exactly as for a typed property write (dot-assign) — full parity,
     *   cross-thread included; returns true. Same-value rewrites of a
     *   non-alwaysNotify field skip observers under both forms.
     * - Declared field, wrong type: rejected without corrupting the field;
     *   returns false.
     * - UNDECLARED name: silently dropped — no error, no ad-hoc field, no
     *   observer fires anywhere — and the return value cannot be trusted:
     *   true from another thread (it reports dispatch, not field acceptance;
     *   verified from the app main thread, expected but not yet exercised
     *   from task threads), false only when called on the node's own thread.
     *
     * Prefer typed property access for `@SG*Field`-annotated fields: the
     * name comes from a validated annotation, so a misnamed field cannot
     * compile. setField is for fields the static type can't express —
     * RoSGNode-typed handles (SDK built-in fields like `control`,
     * `addField`-dynamic fields, generic node code).
     *
     * @param fieldName The name of the field.
     * @param value The value to set.
     * @return True if the write was accepted — but a true from a non-render
     *         thread is NOT proof the field exists (see above).
     */
    public fun setField(fieldName: String, value: Any?): Boolean

    /**
     * Sets multiple field values from an associative array.
     *
     * Carries the same raw-string-name hazard as [setField]: a misnamed key
     * is expected to be silently dropped with no runtime signal (extrapolated
     * from setField's device-verified drop; setFields itself is not yet
     * exercised by the FieldSemantics suite). Prefer typed property access
     * for `@SG*Field`-annotated fields.
     *
     * @param fields An associative array mapping field names to values.
     * @return True if all fields were set successfully.
     */
    public fun setFields(fields: RoAssociativeArray): Boolean

    /**
     * Removes a field from the node.
     *
     * @param fieldName The name of the field to remove.
     * @return True if the field was removed.
     */
    public fun removeField(fieldName: String): Boolean

    /**
     * Removes multiple fields from the node.
     *
     * @param fieldNames An array of field names to remove.
     * @return True if all fields were removed.
     */
    public fun removeFields(fieldNames: RoArray): Boolean

    /**
     * Sets up an observer callback for a field.
     *
     * @param fieldName The name of the field to observe.
     * @param functionName The name of the callback function.
     * @return True if the observer was set up successfully.
     */
    public fun observeField(fieldName: String, functionName: String): Boolean

    /**
     * Sets up a scoped observer callback for a field.
     * The observer is automatically removed when the observing component is destroyed.
     *
     * @param fieldName The name of the field to observe.
     * @param functionName The name of the callback function.
     * @return True if the observer was set up successfully.
     */
    public fun observeFieldScoped(fieldName: String, functionName: String): Boolean

    /**
     * Sets up a message port observer for a field (the port form of observeField).
     * Field changes are delivered as roSGNodeEvents to the given port, waking
     * `port.waitMessage()` on the observing thread.
     *
     * @param fieldName The name of the field to observe.
     * @param port The message port that receives roSGNodeEvents.
     * @return True if the observer was set up successfully.
     */
    @BrsName("observeField")
    public fun observeFieldPort(fieldName: String, port: RoMessagePort): Boolean

    /**
     * Sets up a scoped message port observer for a field (the port form of
     * observeFieldScoped). The observer is automatically removed when the
     * observing component is destroyed.
     *
     * @param fieldName The name of the field to observe.
     * @param port The message port that receives roSGNodeEvents.
     * @return True if the observer was set up successfully.
     */
    @BrsName("observeFieldScoped")
    public fun observeFieldScopedPort(fieldName: String, port: RoMessagePort): Boolean

    /**
     * Removes an observer from a field.
     *
     * @param fieldName The name of the field.
     * @return True if the observer was removed.
     */
    public fun unobserveField(fieldName: String): Boolean

    /**
     * Removes a scoped observer from a field.
     *
     * @param fieldName The name of the field.
     * @return True if the observer was removed.
     */
    public fun unobserveFieldScoped(fieldName: String): Boolean
}

/**
 * Maps to BrightScript's ifSGNodeChildren interface.
 * Provides child node manipulation for SceneGraph nodes.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsgnodechildren.md">ifSGNodeChildren</a>
 */
public external interface ISGNodeChildren {
    /**
     * Appends a child node to this node.
     *
     * @param child The node to append.
     */
    public fun appendChild(child: RoSGNode)

    /**
     * Creates a new child node of the specified type.
     *
     * @param nodeType The type of node to create (e.g., "Label", "Poster", "Group").
     * @return The created child node.
     */
    public fun createChild(nodeType: String): RoSGNode

    /**
     * Inserts a child node at the specified index.
     *
     * @param child The node to insert.
     * @param index The position at which to insert.
     */
    public fun insertChild(child: RoSGNode, index: Int)

    /**
     * Removes a child node.
     *
     * @param child The node to remove.
     */
    public fun removeChild(child: RoSGNode)

    /**
     * Removes the child node at the specified index.
     *
     * @param index The index of the child to remove.
     */
    public fun removeChildIndex(index: Int)

    /**
     * Replaces the child node at the specified index.
     *
     * @param newChild The replacement node.
     * @param index The index of the child to replace.
     */
    public fun replaceChild(newChild: RoSGNode, index: Int)

    /**
     * Gets the child node at the specified index.
     *
     * @param index The index of the child.
     * @return The child node, or null if index is out of bounds.
     */
    public fun getChild(index: Int): RoSGNode?

    /**
     * Gets the parent node of this node.
     *
     * @return The parent node, or null if this is a root node.
     */
    public fun getParent(): RoSGNode?

    /**
     * Gets the number of child nodes.
     *
     * @return The count of children.
     */
    public fun getChildCount(): Int

    /**
     * Moves this node to a new parent.
     *
     * @param newParent The new parent node.
     * @param adjustTransform If true, adjusts the node's transform to maintain visual position.
     * @return True if the reparenting was successful.
     */
    public fun reparent(newParent: RoSGNode, adjustTransform: Boolean): Boolean

    /**
     * Appends multiple child nodes.
     *
     * @param children An array of nodes to append.
     * @return True if all children were appended.
     */
    public fun appendChildren(children: RoArray): Boolean

    /**
     * Inserts multiple child nodes at the specified index.
     *
     * @param children An array of nodes to insert.
     * @param index The position at which to insert.
     * @return True if all children were inserted.
     */
    public fun insertChildren(children: RoArray, index: Int): Boolean

    /**
     * Removes multiple child nodes.
     *
     * @param children An array of nodes to remove.
     * @return True if all children were removed.
     */
    public fun removeChildren(children: RoArray): Boolean

    /**
     * Removes a range of children starting at the specified index.
     *
     * @param numChildren The number of children to remove.
     * @param index The starting index.
     * @return True if the children were removed.
     */
    public fun removeChildrenIndex(numChildren: Int, index: Int): Boolean

    /**
     * Replaces a range of children starting at the specified index.
     *
     * @param children An array of replacement nodes.
     * @param index The starting index.
     * @return True if the children were replaced.
     */
    public fun replaceChildren(children: RoArray, index: Int): Boolean

    /**
     * Gets a range of children starting at the specified index.
     *
     * @param numChildren The number of children to get (-1 for all remaining).
     * @param index The starting index.
     * @return An array of child nodes.
     */
    public fun getChildren(numChildren: Int, index: Int): RoArray

    /**
     * Creates multiple children of the same type.
     *
     * @param numChildren The number of children to create.
     * @param subtype The type of nodes to create.
     * @return An array of the created nodes.
     */
    public fun createChildren(numChildren: Int, subtype: String): RoArray

    /**
     * Gets the Scene node that contains this node.
     *
     * @return The containing Scene node, or null if not in a scene.
     */
    public fun getScene(): RoSGNode?

    /**
     * Updates node fields and children from an associative array.
     *
     * @param aa An associative array describing the updates.
     */
    public fun update(aa: RoAssociativeArray)
}

/**
 * Maps to BrightScript's ifSGNodeDict interface.
 * Provides node lookup and type information for SceneGraph nodes.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsgnodedict.md">ifSGNodeDict</a>
 */
public external interface ISGNodeDict {
    /**
     * Finds a descendant node by ID.
     *
     * Performs a breadth-first search starting from the nearest component ancestor.
     *
     * @param name The ID of the node to find.
     * @return The found node, or null if not found.
     */
    public fun findNode(name: String): RoSGNode?

    /**
     * Gets the subtype of this node.
     *
     * @return The node's subtype (e.g., "Label", "Group").
     */
    public fun subtype(): String

    /**
     * Gets the parent subtype in the SceneGraph hierarchy.
     *
     * @param nodeType The node type to query.
     * @return The parent subtype.
     */
    public fun parentSubtype(nodeType: String): String

    /**
     * Checks if this node is a subtype of another type.
     *
     * @param nodeType The type to check against.
     * @return True if this node's type descends from nodeType.
     */
    public fun isSubtype(nodeType: String): Boolean

    /**
     * Checks if another node reference points to the same node.
     *
     * @param other The other node reference to compare.
     * @return True if both references point to the same SceneGraph node.
     */
    public fun isSameNode(other: RoSGNode): Boolean

    /**
     * Creates a copy of this node.
     *
     * @param deepCopy If true, copies the entire subtree; if false, copies only this node.
     * @return The cloned node.
     */
    public fun clone(deepCopy: Boolean): RoSGNode

    /**
     * Calls a function exposed in the component's interface.
     *
     * The function runs in the component's owning thread (the render thread for
     * scene-parented components, the task thread for Task nodes).
     *
     * @param functionName The name of the interface function to call.
     * @return The function's return value, or null.
     */
    public fun callFunc(functionName: String): Dynamic?

    /**
     * Calls a function exposed in the component's interface with one argument.
     *
     * @param functionName The name of the interface function to call.
     * @param arg The argument to pass to the function.
     * @return The function's return value, or null.
     */
    public fun callFunc(functionName: String, arg: Dynamic?): Dynamic?
}

/**
 * Maps to BrightScript's ifSGNodeFocus interface.
 * Provides focus management for SceneGraph nodes.
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/interfaces/ifsgnodeFocus.md">ifSGNodeFocus</a>
 */
public external interface ISGNodeFocus {
    /**
     * Sets or removes focus on this node.
     *
     * @param on If true, sets focus to this node; if false, removes focus.
     * @return True if the operation was successful.
     */
    public fun setFocus(on: Boolean): Boolean

    /**
     * Checks if this node currently has focus.
     *
     * @return True if this node has remote control focus.
     */
    public fun hasFocus(): Boolean

    /**
     * Checks if this node or any descendant has focus.
     *
     * @return True if this node or any of its descendants has focus.
     */
    public fun isInFocusChain(): Boolean
}

// =============================================================================
// Native Type Interfaces
// =============================================================================

/**
 * Interface representing a native BrightScript roSGScreen.
 *
 * roSGScreen is the main entry point for SceneGraph applications. It creates
 * and manages the SceneGraph rendering surface.
 *
 * Example usage:
 * ```kotlin
 * val screen = RoSGScreen.create()
 * val port = RoMessagePort.create()
 * screen.setMessagePort(port)
 *
 * val scene = screen.createScene("MainScene")
 * screen.show()
 *
 * // Event loop
 * while (true) {
 *     val msg = port.waitMessage(0)
 *     if (msg != null) {
 *         // Handle messages
 *     }
 * }
 *
 * screen.close()
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rosgscreen.md">roSGScreen</a>
 */
public external interface RoSGScreen : ISGScreen, ISetMessagePort, IGetMessagePort {
    /**
     * Creates an instance of a SceneGraph scene component.
     *
     * @param sceneType The name of a component that extends the Scene node class.
     * @return The created Scene node.
     */
    override fun createScene(sceneType: String): RoSGNode

    /**
     * Returns the Scene node created with createScene().
     *
     * @return The Scene node, or null if no scene has been created.
     */
    override fun getScene(): RoSGNode?

    /**
     * Returns the global node for sharing data across all components.
     *
     * @return The global node (m.global).
     */
    override fun getGlobalNode(): RoSGNode

    /**
     * Displays the SceneGraph screen.
     */
    override fun show()

    /**
     * Closes the SceneGraph screen and releases associated resources.
     */
    override fun close()

    public companion object {
        /**
         * Creates a new SceneGraph screen.
         *
         * Compiles to: `CreateObject("roSGScreen")`
         *
         * @return A new RoSGScreen instance.
         */
        @BrsCreateObject("roSGScreen")
        public fun create(): RoSGScreen = definedExternally
    }
}

/**
 * Interface representing a native BrightScript roSGNode.
 *
 * roSGNode is the fundamental building block of SceneGraph applications.
 * It represents any node in the SceneGraph tree, from the Scene itself
 * to individual UI components like Labels, Posters, and Groups.
 *
 * Example usage:
 * ```kotlin
 * // Create a node programmatically
 * val label = RoSGNode.create("Label")
 * label.setField("text", "Hello World")
 * label.setField("font", "font:MediumBoldSystemFont")
 *
 * // Add to parent
 * parentNode.appendChild(label)
 *
 * // Find a node by ID
 * val button = scene.findNode("myButton")
 *
 * // Create child nodes
 * val group = scene.createChild("Group")
 * val poster = group.createChild("Poster")
 * poster.setField("uri", "pkg:/images/logo.png")
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/components/rosgnode.md">roSGNode</a>
 */
public external interface RoSGNode : ISGNodeField, ISGNodeChildren, ISGNodeDict, ISGNodeFocus, IEnumNative {
    // ISGNodeField overrides
    override fun hasField(fieldName: String): Boolean
    override fun getField(fieldName: String): Dynamic?
    override fun getFields(): RoAssociativeArray
    override fun addField(fieldName: String, type: String, alwaysNotify: Boolean): Boolean
    override fun addFields(fields: RoAssociativeArray): Boolean
    override fun getFieldType(fieldName: String): String
    override fun getFieldTypes(): RoAssociativeArray
    override fun setField(fieldName: String, value: Any?): Boolean
    override fun setFields(fields: RoAssociativeArray): Boolean
    override fun removeField(fieldName: String): Boolean
    override fun removeFields(fieldNames: RoArray): Boolean
    override fun observeField(fieldName: String, functionName: String): Boolean
    override fun observeFieldScoped(fieldName: String, functionName: String): Boolean
    @BrsName("observeField")
    override fun observeFieldPort(fieldName: String, port: RoMessagePort): Boolean
    @BrsName("observeFieldScoped")
    override fun observeFieldScopedPort(fieldName: String, port: RoMessagePort): Boolean
    override fun unobserveField(fieldName: String): Boolean
    override fun unobserveFieldScoped(fieldName: String): Boolean

    // ISGNodeChildren overrides
    override fun appendChild(child: RoSGNode)
    override fun createChild(nodeType: String): RoSGNode
    override fun insertChild(child: RoSGNode, index: Int)
    override fun removeChild(child: RoSGNode)
    override fun removeChildIndex(index: Int)
    override fun replaceChild(newChild: RoSGNode, index: Int)
    override fun getChild(index: Int): RoSGNode?
    override fun getParent(): RoSGNode?
    override fun getChildCount(): Int
    override fun reparent(newParent: RoSGNode, adjustTransform: Boolean): Boolean
    override fun appendChildren(children: RoArray): Boolean
    override fun insertChildren(children: RoArray, index: Int): Boolean
    override fun removeChildren(children: RoArray): Boolean
    override fun removeChildrenIndex(numChildren: Int, index: Int): Boolean
    override fun replaceChildren(children: RoArray, index: Int): Boolean
    override fun getChildren(numChildren: Int, index: Int): RoArray
    override fun createChildren(numChildren: Int, subtype: String): RoArray
    override fun getScene(): RoSGNode?
    override fun update(aa: RoAssociativeArray)

    // ISGNodeDict overrides
    override fun findNode(name: String): RoSGNode?
    override fun subtype(): String
    override fun parentSubtype(nodeType: String): String
    override fun isSubtype(nodeType: String): Boolean
    override fun isSameNode(other: RoSGNode): Boolean
    override fun clone(deepCopy: Boolean): RoSGNode
    override fun callFunc(functionName: String): Dynamic?
    override fun callFunc(functionName: String, arg: Dynamic?): Dynamic?

    // ISGNodeFocus overrides
    override fun setFocus(on: Boolean): Boolean
    override fun hasFocus(): Boolean
    override fun isInFocusChain(): Boolean

    public companion object {
        /**
         * Creates a new SceneGraph node of the specified type.
         *
         * Compiles to: `CreateObject("roSGNode", nodeType)`
         *
         * @param nodeType The type of node to create (e.g., "Label", "Poster", "Group").
         * @return A new RoSGNode instance.
         */
        @BrsCreateObject("roSGNode")
        public fun create(nodeType: String): RoSGNode = definedExternally
    }
}

// =============================================================================
// Event Types
// =============================================================================

/**
 * Event received from an roSGScreen.
 *
 * This event is sent to the message port when screen events occur,
 * such as the screen being closed.
 *
 * Example usage:
 * ```kotlin
 * val msg = port.waitMessage(0)
 * if (msg != null && typeOf(msg) == "roSGScreenEvent") {
 *     val event = msg as RoSGScreenEvent
 *     if (event.isScreenClosed()) {
 *         // Exit the application
 *         return
 *     }
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/rosgscreenevent.md">roSGScreenEvent</a>
 */
public external interface RoSGScreenEvent {
    /**
     * Checks if this event indicates the screen was closed.
     *
     * @return True if the screen was closed (typically by pressing the Home button).
     */
    public fun isScreenClosed(): Boolean

    /**
     * Gets the node associated with this event, if any.
     *
     * @return The associated node, or null.
     */
    public fun getNode(): RoSGNode?

    /**
     * Gets additional event information.
     *
     * @return An associative array with event details.
     */
    public fun getInfo(): RoAssociativeArray
}

/**
 * Event received from an roSGNode when an observed field changes.
 *
 * This event is sent to a message port when using the message port
 * version of observeField().
 *
 * Example usage:
 * ```kotlin
 * node.observeField("focusedChild", port)
 *
 * val msg = port.waitMessage(0)
 * if (msg != null && typeOf(msg) == "roSGNodeEvent") {
 *     val event = msg as RoSGNodeEvent
 *     val fieldName = event.getField()
 *     val newValue = event.getData()
 * }
 * ```
 *
 * @see <a href="https://developer.roku.com/docs/references/brightscript/events/rosgnodeevent.md">roSGNodeEvent</a>
 */
public external interface RoSGNodeEvent {
    /**
     * Gets the name of the field that changed.
     *
     * @return The field name.
     */
    public fun getField(): String

    /**
     * Gets the new value of the field.
     *
     * @return The new field value.
     */
    public fun getData(): Dynamic

    /**
     * Gets the id of the node on which the field changed.
     *
     * Note: despite the name, BrightScript's getNode() returns the node's `id`
     * field as a string, NOT a node object (verified on device — see
     * spikes/port-observe-spike/FINDINGS.md, event_accessors). Use
     * [getRoSGNode] to get the node object itself.
     *
     * @return The `id` of the node that generated the event.
     */
    public fun getNode(): String

    /**
     * Gets the roSGNode object on which the field changed.
     *
     * @return The node that generated the event.
     */
    public fun getRoSGNode(): RoSGNode

    /**
     * Gets additional event information.
     *
     * @return An associative array with event details.
     */
    public fun getInfo(): RoAssociativeArray
}
