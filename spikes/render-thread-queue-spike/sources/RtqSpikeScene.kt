package com.nuvyyo.roku.components.rtqspikescene

// Minimal empty scene for the render-thread-queue spike. The driver main()
// creates this scene and then createChild()s the RtqProbe fixtures into it.
class RtqSpikeScene : SceneComponent() {
    init {
        println("RtqSpikeScene: init (render thread)")
    }
}
