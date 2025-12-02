function isInstanceOf(obj as Object, typeName as String) as Boolean
    if obj = invalid then
        return false
    end if
    if Type(obj) <> "roAssociativeArray" then
        return false
    end if
    proto = obj.__proto
    if proto = invalid then
        return false
    end if
    for each t in proto
        if t = typeName then
            return true
        end if
    end for
    return false
end function

sub testArrayList()
    println("=== ArrayList Test ===")
    list = ArrayList_create()
    list.add(1)
    list.add(2)
    list.add(3)
    println("ArrayList size: " + list.get_size().toString())
    println("First element: " + list[0].toString())
end sub

sub testHashMap()
    println("=== HashMap Test ===")
    map = HashMap_create()
    map.put("one", 1)
    map.put("two", 2)
    println("HashMap size: " + map.get_size().toString())
    println("Value for 'one': " + toString(map.get("one")))
end sub

sub testUnsigned()
    println("=== Unsigned Types Test ===")
    a = 42
    b = 8
    println("42u value: " + a.toString())
    println("8u value: " + b.toString())
end sub

sub testRanges()
    println("=== Ranges Test ===")
    count = 0
    for each i in 1.rangeTo(5)
        "/* Unsupported: IrSetValueImpl */"
    end for
    println("Counted 1..5: " + count.toString())
end sub

sub testPair()
    println("=== Pair Test ===")
    pair = Pair_create(1, "one")
    println("Pair first: " + pair.get_first().toString())
    println("Pair second: " + pair.get_second())
end sub

sub main()
    println("BrightScript Stdlib Basic Tests")
    println("================================")
    testArrayList()
    testHashMap()
    testUnsigned()
    testRanges()
    testPair()
    println("================================")
    println("Tests Completed!")
end sub
