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

function add(a as Integer, b as Integer) as Integer
    return a + b
end function

function greet(name as String) as String
    return "Hello, " + name
end function

sub main()
    result = add(1, 2)
    message = greet("Roku")
end sub
