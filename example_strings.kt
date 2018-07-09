fun hello(name: String, lang: String?) = "${if (lang ?: "" == "") HELLO_EN else lang} $name!!!\n"

fun helloWorld(): Boolean {
    println("Hello $EXAMPLE_WORLD!!!")
    return true;
}

fun helloOrNotHello(hello: Boolean) = println(if (hello) "Hello!" else "Nop!")