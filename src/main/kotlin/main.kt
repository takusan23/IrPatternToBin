/**
 * NECの赤外線パターンを２進数に変換する？
 *
 * # 赤外線パターン構造
 *
 * +------------------------------------------
 *
 * | リーダー部 | データ部 ... | ストップビット
 *
 * +------------------------------------------
 *
 * # メモ
 *
 * ## ON / OFF
 * 配列の奇数の添字がON、偶数の添字がOFF。
 *
 * 0、1、3がON、2、4、6がOFF
 *
 * ## Tの値
 * Tは562。( 9/16 の計算結果)。なおNEC以外は知らん。
 *
 * 後述するリーダー部の16Tってのは、16*Tって意味。
 *
 * ## 反転
 *
 * 1010 -> 0101 ってことね。0と1を反転させる。
 *
 * # リーダー部
 * 配列で見て、0,1要素目の謎に数字がでかい部分は「リーダー部」
 *
 * おおよそ、ONが9000(T*16)、OFFが4500(T*8) ぐらいになるはず。
 *
 * # データ部
 * 配列からみて3要素目からデータ部。32ビットで出来ている。
 *
 * ON / OFF を比べて、OFFのほうがONより2倍以上（定義ではTの3倍）あったときは２進数の1、
 *
 * そうじゃないときは２進数の0。
 *
 * これをつなげていく。
 *
 * ## データ部の中身。カスタマーコード編
 *
 * 16ビットで区切って前の16ビットがカスタマーコード。
 *
 * ## データ部の中身。データ部
 *
 * データ部からカスタマーコードの16ビットを取って残った16ビットがデータ。
 *
 * これも最初の8ビットの反転が最後の8ビットになっているらしい。
 *
 * # ストップビット部
 *
 * 最後の T*1 を足せば終わり。
 *
 * # 参考リンク
 *
 * http://shrkn65.nobody.jp/remocon/nec.html
 *
 * http://elm-chan.org/docs/ir_format.html
 *
 * https://netlog.jpn.org/r271-635/2014/07/ir_remotecon_sender_test.html
 *
 * http://www.256byte.com/2006/10/post_13.html
 * */
fun main() {

    /**
     * 赤外線信号のパターン
     * */
    val list = intArrayOf(

        // 各自ここに赤外線パターンを入れてくれ。

    )

    /** 切り替えよう */
    val patternList = list

    /** 変調。NECなら562。 */
    val necT = 562

    /**
     * 1,2番目をPair、3,4番目もPairにしていく
     *
     * 変換例
     *
     * [(8955, 4510),(607, 520)]
     * */
    val onOffPairList = patternToOnOffPairList(patternList.toList())

    /**
     * ２進数変換。ONとOFFが1:3の比率（だいたい）の場合は1、違う場合は0になる
     *
     * 32bit（32文字数になる）、ハズ
     *
     * リーダー部（9000,4500）（だいたいONが9000、OFFが4500）の次からがデータ部なので
     *
     * 例
     *
     * (607, 520) =（ON607、OFF520） なら 0
     * (607, 1703) =（ON607、OFF1703）なら 1
     *
     * 変換例
     * 010000 ...
     * */
    val binCode = patternToBinCode(patternList.toList())

    /** ２進数からパターン生成 */
    val makingPattern = binCodeToPattern(necT, binCode)

    println("--- 入力データ ---")
    onOffPairList.forEach { println(it) }
    println("赤外線ON,OFF配列    ：${patternList.toList()}")
    println("ON:OFFのPair配列  　：${onOffPairList}")
    println("--- パターン構造 ---")
    println("リーダー（先頭）   ：${onOffPairList.subList(0, 2)}）")
    println("カスタマーコード   ：${onOffPairList.subList(3, 16)}）")
    println("データ            ：${onOffPairList.subList(17, 33)}）")
    println("--- パターンから２進数へ ---")
    println("リーダー (先頭)      ：${onOffPairList[0]}")
    println("データ部２進数       ：$binCode , length = ${binCode.length}")
    println("カスタマーコード     ：${binCode.substring(0..15)}")
    println("データ              ：${binCode.substring(16..31)}")
    println("8bit データ         ：${binCode.substring(16..23)}")
    println("8bit データ 反転    ：${binCode.substring(24..31)}")
    println("--- ２進数からパターン生成 ---")
    val remakeBinCode = patternToBinCode(makingPattern)
    println("もとのパターン   　 ：${patternList.toList()}")
    println("２進数->パターン生成：${makingPattern}")
    println("もとのデータ部２進数：${binCode} , length = ${binCode.length}")
    println("生成後データ部２進数：${remakeBinCode} , length = ${remakeBinCode.length}")

}

/**
 * [8955, 4510]を[Pair(8955, 4510)]にしていく関数。戻す際はflatMapを使ってください。
 * */
fun patternToOnOffPairList(patternList: List<Int>) =
    patternList
        .toMutableList()
        .mapIndexed { index, _ ->
            if ((index + 1) % 2 != 0) {
                Pair(patternList[index], patternList.getOrNull(index + 1) ?: 0)
            } else null
        }
        .filterNotNull()

/**
 * [patternList]のデータ部を2進数にして返す。
 *
 * ONとOFFが1:3の比率（だいたい）の場合は1、違う場合は0になる
 *
 * 32bit(32文字)になるはず
 *
 * リーダー部（9000,4500）（だいたいONが9000、OFFが4500）の次からがデータ部なので
 *
 * 例
 *
 * (607, 520) =（ON607、OFF520） なら 0
 * (607, 1703) =（ON607、OFF1703）なら 1
 *
 * 変換例
 * 010000 ...
 *
 * @param patternList ON/OFFパターン配列
 * */
fun patternToBinCode(patternList: List<Int>) =
    patternToOnOffPairList(patternList)
        .drop(1) // リーダー部を消す
        .dropLast(1) // ストップビット部も消す
        .map { (on, off) -> if (off > on * 2) "1" else "0" } // ONの2倍以上で T*3 ってことで
        .joinToString(separator = "") { it }

/**
 * ２進数からパターン生成。先頭にトレーラーつけて、最後にストップビットを入れる
 *
 * @param t 変調。NECなら 562 前後？
 * @param binCode ２進数
 * */
fun binCodeToPattern(t: Int, binCode: String) =
    listOf(t * 16, t * 9) + binCode.toList().flatMap { if (it == '1') listOf(t * 1, t * 3) else listOf(t * 1, t * 1) } + listOf(t * 1) // 1なら[T*1,T*3]、0なら[T*1,T*1]を配列に足していく
