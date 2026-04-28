// Expected: no diagnostic.
//
// CRITICAL ORDER-SWAP CANARY: this fixture has no @BrsConstant annotation. If the annotation
// gate does not run first (i.e., if classKind is checked before the annotation lookup), then
// every non-object class would incorrectly enter the NON_OBJECT report branch. This fixture
// failing with an unexpected BRS_BRSCONSTANT_NON_OBJECT means the order-swap regressed.

class Plain {
    var x = 1
    val y = 2
}
