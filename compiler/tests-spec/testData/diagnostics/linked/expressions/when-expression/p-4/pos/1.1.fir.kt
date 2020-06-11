// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT
// FULL_JDK

// FILE: JavaEnum.java

enum JavaEnum {
    Val_1,
    Val_2,
}

// FILE: KotlinClass.kt

// TESTCASE NUMBER: 1
fun case1() {
    val z = JavaEnum.Val_1
    val when1 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
        else -> {}
    }

    val when2 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
    }
    val when3 = when (z) {
        JavaEnum.Val_1 -> { }
        JavaEnum.Val_2 -> { }
        JavaEnum.Val_2 -> { }
    }
}

// TESTCASE NUMBER: 2

fun case2() {
    val b = false
    val when1: Any = when (b) {
        false -> { }
        !false -> { }
        else -> { }
    }

    val when2: Any = when (b) {
        false -> { }
        !false -> { }
    }
    val when3: Any = when (b) {
        false -> { }
        false -> { }
        !false -> { }
    }
}

// TESTCASE NUMBER: 3

fun case3() {
    val a = false
    val when1: Any = when (a) {
        true -> { }
        false -> { }
        else -> { }
    }
    val when2: Any = when (a) {
        true -> { }
        false -> { }
    }
    val when3: Any = when (a) {
        true -> { }
        false -> { }
        false -> { }
    }
}

// TESTCASE NUMBER: 4

fun case4() {
    val x: SClass = SClass.B()

    val when1 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  SClass.C ->{ }
        else -> { }
    }

    val when2 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  SClass.C ->{ }
    }
    val when3 = when (x){
        is  SClass.A ->{ }
        is  SClass.B ->{ }
        is  SClass.B ->{ }
        is  SClass.C ->{ }
    }
}

sealed class SClass {
    class A : SClass()
    class B : SClass()
    class C : SClass()
}