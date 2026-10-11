package com.sriramanappindi.openwire.games

import kotlin.random.Random

/**
 * One multiple-choice general-knowledge question. [correctIndex] indexes
 * into [options]. Every question here is a stable, evergreen fact (capitals,
 * basic science, classic history/literature) rather than anything that could
 * go stale — there's no backend to update this list once the app ships, so a
 * question whose "correct" answer changes over time (a current officeholder,
 * a record that gets broken) would quietly go wrong with no way to fix it.
 */
data class GkQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val category: String
)

object GkQuiz {
    /** How many questions make up one day's round. */
    const val QUESTIONS_PER_DAY = 5

    val ALL: List<GkQuestion> = listOf(
        GkQuestion("gk-001", "What is the capital of Australia?", listOf("Sydney", "Melbourne", "Canberra", "Perth"), 2, "Geography"),
        GkQuestion("gk-002", "What is the capital of Canada?", listOf("Toronto", "Vancouver", "Ottawa", "Montreal"), 2, "Geography"),
        GkQuestion("gk-003", "What is the capital of Japan?", listOf("Osaka", "Tokyo", "Kyoto", "Yokohama"), 1, "Geography"),
        GkQuestion("gk-004", "What is the capital of Egypt?", listOf("Cairo", "Alexandria", "Giza", "Luxor"), 0, "Geography"),
        GkQuestion("gk-005", "What is the capital of Italy?", listOf("Milan", "Venice", "Naples", "Rome"), 3, "Geography"),
        GkQuestion("gk-006", "What is the capital of France?", listOf("Lyon", "Paris", "Marseille", "Nice"), 1, "Geography"),
        GkQuestion("gk-007", "Which is the largest ocean on Earth?", listOf("Atlantic", "Indian", "Arctic", "Pacific"), 3, "Geography"),
        GkQuestion("gk-008", "Which river is traditionally considered the longest in the world?", listOf("Amazon", "Nile", "Yangtze", "Mississippi"), 1, "Geography"),
        GkQuestion("gk-009", "Which is the tallest mountain above sea level?", listOf("K2", "Kangchenjunga", "Mount Everest", "Lhotse"), 2, "Geography"),
        GkQuestion("gk-010", "How many continents are traditionally recognized?", listOf("5", "6", "7", "8"), 2, "Geography"),
        GkQuestion("gk-011", "Which is the largest country in the world by area?", listOf("Canada", "China", "USA", "Russia"), 3, "Geography"),
        GkQuestion("gk-012", "Which is the smallest country in the world by area?", listOf("Monaco", "Vatican City", "San Marino", "Liechtenstein"), 1, "Geography"),
        GkQuestion("gk-013", "The Great Wall is located in which country?", listOf("Japan", "Mongolia", "China", "South Korea"), 2, "Geography"),
        GkQuestion("gk-014", "The Eiffel Tower is located in which city?", listOf("London", "Paris", "Rome", "Berlin"), 1, "Geography"),
        GkQuestion("gk-015", "The Taj Mahal is located in which Indian city?", listOf("Delhi", "Jaipur", "Agra", "Mumbai"), 2, "Geography"),
        GkQuestion("gk-016", "The Statue of Liberty is located in which city?", listOf("Boston", "New York", "Washington D.C.", "Philadelphia"), 1, "Geography"),
        GkQuestion("gk-017", "What is the chemical symbol for gold?", listOf("Ag", "Gd", "Au", "Go"), 2, "Science"),
        GkQuestion("gk-018", "What is the chemical symbol for oxygen?", listOf("Ox", "O", "Og", "O2"), 1, "Science"),
        GkQuestion("gk-019", "What is the chemical formula for water?", listOf("CO2", "H2O", "NaCl", "O2"), 1, "Science"),
        GkQuestion("gk-020", "Which planet is known as the Red Planet?", listOf("Venus", "Jupiter", "Mars", "Saturn"), 2, "Science"),
        GkQuestion("gk-021", "Which is the largest planet in our solar system?", listOf("Saturn", "Jupiter", "Neptune", "Uranus"), 1, "Science"),
        GkQuestion("gk-022", "Which planet is closest to the Sun?", listOf("Venus", "Earth", "Mercury", "Mars"), 2, "Science"),
        GkQuestion("gk-023", "What is the freezing point of water in Celsius?", listOf("0°C", "32°C", "100°C", "-10°C"), 0, "Science"),
        GkQuestion("gk-024", "What is the boiling point of water in Celsius at sea level?", listOf("90°C", "100°C", "110°C", "212°C"), 1, "Science"),
        GkQuestion("gk-025", "What is commonly called the \"powerhouse of the cell\"?", listOf("Nucleus", "Ribosome", "Mitochondria", "Chloroplast"), 2, "Science"),
        GkQuestion("gk-026", "Which gas do plants absorb from the atmosphere for photosynthesis?", listOf("Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen"), 2, "Science"),
        GkQuestion("gk-027", "Which is the first element on the periodic table?", listOf("Helium", "Hydrogen", "Lithium", "Carbon"), 1, "Science"),
        GkQuestion("gk-028", "Who developed the theory of general relativity?", listOf("Isaac Newton", "Niels Bohr", "Albert Einstein", "Galileo Galilei"), 2, "Science"),
        GkQuestion("gk-029", "Who is credited with inventing the telephone?", listOf("Thomas Edison", "Nikola Tesla", "Alexander Graham Bell", "Guglielmo Marconi"), 2, "Science"),
        GkQuestion("gk-030", "Who is commonly credited with inventing the practical light bulb?", listOf("Thomas Edison", "Alexander Graham Bell", "Benjamin Franklin", "James Watt"), 0, "Science"),
        GkQuestion("gk-031", "How many bones are in an adult human skeleton?", listOf("186", "206", "226", "246"), 1, "Science"),
        GkQuestion("gk-032", "Which is the hardest naturally occurring substance on Earth?", listOf("Quartz", "Diamond", "Granite", "Titanium"), 1, "Science"),
        GkQuestion("gk-033", "Which is the largest mammal in the world?", listOf("African elephant", "Blue whale", "Giraffe", "Sperm whale"), 1, "Science"),
        GkQuestion("gk-034", "Which is the fastest land animal?", listOf("Lion", "Greyhound", "Cheetah", "Pronghorn antelope"), 2, "Science"),
        GkQuestion("gk-035", "How many hearts does an octopus have?", listOf("1", "2", "3", "4"), 2, "Science"),
        GkQuestion("gk-036", "Who wrote \"Romeo and Juliet\"?", listOf("Charles Dickens", "William Shakespeare", "Jane Austen", "Mark Twain"), 1, "Literature"),
        GkQuestion("gk-037", "Who wrote \"Pride and Prejudice\"?", listOf("Emily Brontë", "Jane Austen", "Virginia Woolf", "George Eliot"), 1, "Literature"),
        GkQuestion("gk-038", "Who painted the Mona Lisa?", listOf("Michelangelo", "Raphael", "Leonardo da Vinci", "Donatello"), 2, "Art"),
        GkQuestion("gk-039", "Who was the first person to walk on the Moon?", listOf("Buzz Aldrin", "Yuri Gagarin", "Neil Armstrong", "John Glenn"), 2, "History"),
        GkQuestion("gk-040", "In which year did World War II end?", listOf("1943", "1944", "1945", "1946"), 2, "History"),
        GkQuestion("gk-041", "In which year did World War I begin?", listOf("1912", "1914", "1916", "1918"), 1, "History"),
        GkQuestion("gk-042", "How many players does each soccer team have on the field?", listOf("9", "10", "11", "12"), 2, "Sports"),
        GkQuestion("gk-043", "How many players does each basketball team have on the court?", listOf("4", "5", "6", "7"), 1, "Sports"),
        GkQuestion("gk-044", "Wimbledon is a famous tournament in which sport?", listOf("Golf", "Cricket", "Tennis", "Rugby"), 2, "Sports"),
        GkQuestion("gk-045", "How many rings are on the Olympic flag?", listOf("4", "5", "6", "7"), 1, "Sports"),
        GkQuestion("gk-046", "What is the official currency of Japan?", listOf("Won", "Yuan", "Yen", "Ringgit"), 2, "General"),
        GkQuestion("gk-047", "What is the official currency of the United Kingdom?", listOf("Euro", "Pound sterling", "Dollar", "Franc"), 1, "General"),
        GkQuestion("gk-048", "What is the primary official language of Brazil?", listOf("Spanish", "Portuguese", "French", "Italian"), 1, "General"),
        GkQuestion("gk-049", "How many sides does a hexagon have?", listOf("5", "6", "7", "8"), 1, "Mathematics"),
        GkQuestion("gk-050", "What is the square root of 64?", listOf("6", "7", "8", "9"), 2, "Mathematics"),
    )

    /** Same five questions for everyone on a given day, like the other daily games. */
    fun forDay(epochDay: Long): List<GkQuestion> {
        val rng = Random(epochDay)
        return ALL.shuffled(rng).take(QUESTIONS_PER_DAY)
    }
}
