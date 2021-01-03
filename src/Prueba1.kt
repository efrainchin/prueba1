import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.grapecity.documents.excel.Color
import com.grapecity.documents.excel.Workbook
import res.model.Transaction
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.text.ParseException
import java.util.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.stream.Collectors.groupingBy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.HashMap
import kotlin.math.round

fun main(args: Array<String>){
        val workbook = Workbook()
        val worksheet = workbook.worksheets.get(0)
        val positionCell = arrayOf("B","C","D","E","F","G","H","I","J","K","K","M","N")
        try {
                val groupMonth = getGroupByMonth("src/res/transactions.json").values.toList()
                for (position in groupMonth.indices) {
                        val months = arrayListOf<String>()
                        val group = groupMonth[position]
                        var row = 3
                        months.add(getMonthName(group.first().creation_date))
                        months.add("${group.count { it.status == "pending" }} transaciones pendientes")
                        months.add("${group.count { it.status == "rejected" }} transaciones bloqueadas")
                        val totalIncome = group.filter { it.operation == "in" && it.status == "done"}.sumByDouble { it.amount }.round(2)
                        months.add("$$totalIncome ingresos")
                        val totalExpenses = group.filter { it.operation == "out" && it.status == "done"}.sumByDouble { it.amount }.round(2)
                        months.add("$$totalExpenses gastos")
                        val listNameTransactions = group.filter { it.operation == "out" }.distinctBy { it.category }.map { it.category }
                        val listOutTransactions = group.filter { it.operation == "out" }
                        val mapTransaction = HashMap<String,Double>()
                        for (index in listNameTransactions.indices){
                                val numTransaction = listOutTransactions.count { it.category == listNameTransactions[index]}
                                val percentaje = listOutTransactions.size / 100.0 * numTransaction
                                mapTransaction[listNameTransactions[index]] = percentaje.round(2)
                        }
                        val sortByPercentajeTransaction = mapTransaction.entries.sortedByDescending { it.value }.associate { it.toPair() }
                        sortByPercentajeTransaction.forEach {
                                months.add("${it.key} %${it.value}")
                        }
                        months.forEach {
                                if (row == 3)
                                        worksheet.getRange("${positionCell[position]}$row").font.color = Color.GetBlack()
                                worksheet.getRange("${positionCell[position]}$row").columnWidth = 30.0
                                worksheet.getRange("${positionCell[position]}$row").value = it
                                row++
                        }
                }
                workbook.save("Report.xlsx")
        } catch (e: FileNotFoundException) {
                System.out.println("Archivo no valido")
        } catch (e: JsonSyntaxException) {
                System.out.println("Json invalido")
        } catch (e: Exception) {
                System.out.println("Error desconocido ${e.localizedMessage}")
        }
}

@Throws(ParseException::class)
private fun getMonthName(date: String): String {
        val d = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).parse(date)
        val cal = Calendar.getInstance()
        cal.time = d
        return SimpleDateFormat("MMMM").format(cal.time)
}

private fun  openFileConverToString(path: String): String {
        val file = File(path)
        var stringFile = ""
        val input = Scanner(file)
        while (input.hasNextLine()) {
                stringFile+=input.nextLine()
        }
        input.close()
        return stringFile
}

private fun convertStringToListTransaction(input: String): List<Transaction> {
        val myType = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson<List<Transaction>>(input, myType)
}

private fun sortByDates(list: List<Transaction>): List<Transaction> =
        list.sortedBy { it.creation_date }

private fun getGroupByMonth(path: String): Map<Int, List<Transaction>>{
        val list = sortByDates(convertStringToListTransaction(openFileConverToString(path)))
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        val byMonth: Map<Int, List<Transaction>> = list.stream()
                .collect(groupingBy { d -> LocalDate.parse(d.creation_date,formatter).get(ChronoField.MONTH_OF_YEAR)})
        return byMonth
}
private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
}