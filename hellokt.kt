fun kt_print(string: String) {
    konan.initRuntimeIfNeeded()
    println("Hello, $string!!!")
}