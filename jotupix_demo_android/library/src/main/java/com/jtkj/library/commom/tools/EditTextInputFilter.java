package com.jtkj.library.commom.tools;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* 过滤EditText输入字符
*/
public class EditTextInputFilter implements InputFilter {
    private static final String REGEX_ZH = "^[\\u4e00-\\u9fa5]+$";
    private static final String REGEX_EMO ="^[\\u0000-\\ud7a3\\uff01-\\uffee]+$";
    // http://www.fhdq.net/ 符号大全
    private static final String REGEX="^[a-zA-Z0-9`~!@#$%^&*()+=_\"-|{}':;,\\[\\].<>/?！￥‹›〔〕|︵︷︹︿︽﹁﹃︻︗∶…（）—【】「」〖〗『』·～＆＠＃《》＜＞﹝﹞〈〉‘；：”“’。，、？" +
            "❤❥웃유♋☮✌☏☢☠✔☑♚▲♪✈✞÷↑↓◆◇⊙■□△▽¿─│♥❣♂♀☿Ⓐ✍✉☣☤✘☒♛▼♫⌘☪≈←→◈◎☉★☆⊿※¡━┃♡ღツ☼☁❅♒✎©®™Σ✪✯☭➳卐√↖↗●◐Θ◤◥┄┆℃℉°✿ϟ☃☂✄¢€£∞✫½✡×↙↘○◑⊕◣◢︼" +
            "┅┇☽☾✚〓▂▃▄▅▆▇█▉▊▋▌▍▎▏↔↕の•▸◂▴▾┈┊①②③④⑤⑥⑦⑧⑨⑩ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩ㍿▓♨❖♓✙┉┋☹☺☻تヅッシÜϡﭢ℠℗❄☈" +
            "✺☇♤♧♢♠♣♦☜☞☝☚☛☟✽✾❁❃❋❀⚘✓☐✗ㄨ✕✖⋆✢✣✤✥✦✧✩✰✬✭✮❂✱✲✳✴✵✶✷✸✹✻✼❆❇❈❉❊†☨✝☥☦☓☩☯☧☬☸♁♆＇〝〞ˆˇ﹕︰﹔﹖" +
            "﹑¨¸ˉ｜‖＂〃｀﹫﹏﹋﹌︴々﹟﹩﹠﹪﹡﹢﹦﹤‐￣¯―﹨˜﹍﹎＿_-~﹉﹊﹛﹜［］︶︸﹀︺︾﹂﹄✛✜✠‡◉◌◍" +
            "◒◓◔◕◖◗⊗◘◙⅟⅓⅕⅙⅛⅔⅖⅚⅜¾⅗⅝⅞⅘≂≃≄≅≆≇≉≊≋≌≍≎≏≐≑≒≓≔≕≖≗≘≙≚≛≜≝≞≟≠≡≢≣≤≥≦≧≨≩⊰⊱⋛⋚∫∬∭∮∯∰∱∲∳℅‰‱㊣㊎㊍㊌㊋㊏㊐㊊㊚㊛㊤㊥㊦㊧㊨㊒㊞㊑㊓㊔㊕㊖㊗㊘㊜㊝㊟㊠㊡㊢㊩㊪㊫" +
            "㊬㊭㊮㊯㊰㊙㉿囍♔♕♖♗♘♙♜♝♞♟ℂℍℕℙℚℝℤℬℰℯℱℊℋℎℐℒℓℳℴ℘ℛℭ℮ℌℑℜℨ♩♬♭♮♯✐✏✑✒✁✂✃✆☎➟➡➢➣➤➥➦➧➨➚➘➙➛➜➝➞➸♐➲⏎➴➵➶➷➹➺➻➼➽↚↛↜↝↞↟↠↡↢↣↤↥↦↧↨➫➬➩➪➭➮➯➱↩↪↫↬↭↮↯↰↱↲↳" +
            "↴↵↶↷↸↹↺↻↼↽↾↿⇀⇁⇂⇃⇄⇅⇆⇇⇈⇉⇊⇋⇌⇍⇎⇏⇐⇑⇒⇓⇔⇕⇖⇗⇘⇙⇚⇛⇜⇝⇞⇟⇠⇡⇢⇣⇤⇥⇦⇧⇨⇩⇪➀➁➂➃➄➅➆➇➈➉➊➋➌➍➎➏➐➑➒➓㊀㊁㊂㊃㊄㊅㊆㊇㊈㊉ⒷⒸⒹⒺⒻⒼⒽⒾⒿⓀⓁⓂⓃⓄⓅⓆⓇⓈⓉⓊⓋⓌⓍⓎⓏⓐⓑⓒⓓⓔⓕⓖⓗⓘⓙⓚⓛ" +
            "ⓜⓝⓞⓟⓠⓡⓢⓣⓤⓥⓦⓧⓨⓩ⒜⒝⒞⒟⒠⒡⒢⒣⒤⒥⒦⒧⒨⒩⒪⒫⒬⒭⒮⒯⒰⒱⒲⒳⒴⒵ⅪⅫⅬⅭⅮⅯⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹⅺⅻⅼⅽⅾⅿ┌┍┎┏┐┑┒┓└┕┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩┪┫┬┭┮┯┰┱┲┳┴┵┶" +
            "┷┸┹┺┻┼┽┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬◄►▶◀▷◁▻◅▵▿▹◃❏❐❑❒▀▁▐░▒▔▕▢▣▤▥▦▧▨▩▪▫▬▭▮▯﹣±∽﹥≮≯∷∝∧∨∑∏∪∩∈∵∴⊥∥∠⌒∟㏒㏑Ø" +
            "π´_-ˇ❝❞′″＄〒￠￡％㎡㏕㎜㎝㎞㏎m³㎎㎏㏄º¤¹²Ұ₴₰₤¥₳₲₪₵元₣₱฿₡₮₭₩ރ円₢₥₫₦zł﷼₠₧₯₨Kčर₹ƒ₸«»➾➔➠☰☲☱☴☵☶☳☷☀卍◊◦あぃЮ§❦❧☄☊☋☌☍۰♈큐　‿-｡｡‿╳╮╭╯╰āáǎàōóǒòēéěèīíǐìūúǔùǖǘǚǜüêɑ\uE7C7ńň\uE7C8ɡㄅㄆㄇㄈ" +
            "ㄉㄊㄋㄌㄍㄎㄏㄐㄑㄒㄓㄔㄕㄖㄗㄘㄙㄚㄛㄜㄝㄞㄟㄠㄡㄢㄣㄤㄥㄦㄧㄩ⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳⓪❶❷❸❹❺❻❼❽❾❿⓫⓬⓭⓮⓯⓰⓱⓲⓳⓴㈠㈡㈢㈣㈤㈥㈦㈧㈨㈩⑴⑵⑶⑷⑸⑹⑺⑻⑼⑽⑾⑿⒀⒁⒂⒃⒄⒅⒆⒇⒈⒉⒊⒋⒌⒍⒎⒏⒐⒑⒒⒓⒔⒕⒖⒗⒘⒙⒚⒛" +
            "№ΦΨ¶ΑΒΓΔΕΖΗΙΚΛΜΝΞΟΠΡΤΥΧΩαβγδεζνξορσηθικλμτυφχψω]+$";

    public EditTextInputFilter() {
        super();
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (!isValidChars(source) || isContainZh(source)){
            return "";
        }
        return source;
    }

    /**
     * 是否包含中文
     * @param input 输入
     * @return true，匹配到中文
     */
    private  boolean isContainZh(CharSequence input){
        return isMatch(REGEX_ZH, input);
    }

    /**
     * 是否包含表情
     *
     * @param input 输入
     * @return true，匹配到表情
     */
    private  boolean isContainEmo(CharSequence input) {
        Pattern pattern = Pattern.compile(REGEX_EMO);
        Matcher matcher = pattern.matcher(input);
        return !matcher.find();
    }

    private  boolean isValidChars(CharSequence input) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(input);
        return matcher.find();
    }

    private  boolean isMatch(String regex, CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }
}
