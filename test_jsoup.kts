@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("org.jsoup:jsoup:1.15.3")
import org.jsoup.Jsoup

val doc = Jsoup.connect("https://www.thomasjacquin.com/allsky/index.php?page=system").get()
val info = mutableMapOf<String, String>()
doc.select("tr").forEach { row ->
    val cols = row.select("td")
    if (cols.size >= 2) {
        val key = cols[0].text().trim().trimEnd(':')
        val value = cols[1].text().trim()
        if (key.isNotEmpty() && value.isNotEmpty()) {
            info[key] = value
        }
    }
}
println(info)
