package com.nuvyyo.roku.components.spikescene

// Minimal empty scene for the port-observe spike. The driver main() creates
// this scene and then createChild()s the PortProbe fixture into it.
class SpikeScene : SceneComponent() {
    init {
        println("SpikeScene: init (render thread)")
    }
}
