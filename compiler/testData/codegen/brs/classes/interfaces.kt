// Interfaces test
interface Describable {
    fun describe(): String
}

interface Named {
    fun getName(): String
}

class Product(val name: String, val price: Int) : Describable {
    override fun describe(): String {
        return "$name costs $price"
    }
}

class Item(val title: String, val value: Int) : Describable, Named {
    override fun describe(): String {
        return "Item: $title"
    }

    override fun getName(): String {
        return title
    }
}

fun getDescription(item: Describable): String {
    return item.describe()
}

fun createProduct(): Product {
    return Product("Widget", 100)
}
