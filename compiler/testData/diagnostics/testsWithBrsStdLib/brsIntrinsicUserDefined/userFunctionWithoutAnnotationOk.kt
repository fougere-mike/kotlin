// Expected: no diagnostic — user function without @BrsIntrinsic is fine
fun regularFun(): Int = 42

external fun externalWithoutAnnotation(): Int
