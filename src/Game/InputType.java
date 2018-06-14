package Game;

import Data.SerializationVersion;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;

public class InputType implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /*
    InputType:

    Essentially a struct that encapsulates an input code (key or mouse) and identifies itself as either keyboard or mouse input.
     */

    private int inputCode; //The integer 'code' of the input

    private int inputType; //The type of input
    static final int TYPE_KEY   = 0;
    static final int TYPE_MOUSE = 1;

    public InputType(int code, int type){
        inputCode = code;
        inputType = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InputType) {
            InputType type = (InputType) obj;
            return type.inputCode == inputCode && type.inputType == inputType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (65537 * inputType) + inputCode;
    }

    @Override
    public String toString() {
        if (inputType == TYPE_KEY) {
            switch (inputCode) {
                case KeyEvent.VK_0:
                    return "0";
                case KeyEvent.VK_1:
                    return "1";
                case KeyEvent.VK_2:
                    return "2";
                case KeyEvent.VK_3:
                    return "3";
                case KeyEvent.VK_4:
                    return "4";
                case KeyEvent.VK_5:
                    return "5";
                case KeyEvent.VK_6:
                    return "6";
                case KeyEvent.VK_7:
                    return "7";
                case KeyEvent.VK_8:
                    return "8";
                case KeyEvent.VK_9:
                    return "9";
                case KeyEvent.VK_A:
                    return "A";
                case KeyEvent.VK_ACCEPT:
                    return "ACCEPT";
                case KeyEvent.VK_ADD:
                    return "NUM+";
                case KeyEvent.VK_AGAIN:
                    return "AGAIN";
                case KeyEvent.VK_ALL_CANDIDATES:
                    return "ALLCAN";
                case KeyEvent.VK_ALPHANUMERIC:
                    return "ALPHNU";
                case KeyEvent.VK_ALT:
                    return "ALT";
                case KeyEvent.VK_ALT_GRAPH:
                    return "ALTGRA";
                case KeyEvent.VK_AMPERSAND:
                    return "&";
                case KeyEvent.VK_ASTERISK:
                    return "*";
                case KeyEvent.VK_AT:
                    return "@";
                case KeyEvent.VK_B:
                    return "B";
                case KeyEvent.VK_BACK_QUOTE:
                    return "`";
                case KeyEvent.VK_BACK_SLASH:
                    return "\\";
                case KeyEvent.VK_BACK_SPACE:
                    return "BKSP";
                case KeyEvent.VK_BEGIN:
                    return "BEGIN";
                case KeyEvent.VK_BRACELEFT:
                    return "{";
                case KeyEvent.VK_BRACERIGHT:
                    return "}";
                case KeyEvent.VK_C:
                    return "C";
                case KeyEvent.VK_CANCEL:
                    return "CANCEL";
                case KeyEvent.VK_CAPS_LOCK:
                    return "CAPS";
                case KeyEvent.VK_CIRCUMFLEX:
                    return "CIRCUM";
                case KeyEvent.VK_CLEAR:
                    return "CLEAR";
                case KeyEvent.VK_CLOSE_BRACKET:
                    return "]";
                case KeyEvent.VK_CODE_INPUT:
                    return "CODEIN";
                case KeyEvent.VK_COLON:
                    return ":";
                case KeyEvent.VK_COMMA:
                    return ",";
                case KeyEvent.VK_COMPOSE:
                    return "COMPOS";
                case KeyEvent.VK_CONTEXT_MENU:
                    return "CNTXT";
                case KeyEvent.VK_CONTROL:
                    return "CTRL";
                case KeyEvent.VK_CONVERT:
                    return "CNVERT";
                case KeyEvent.VK_COPY:
                    return "COPY";
                case KeyEvent.VK_CUT:
                    return "CUT";
                case KeyEvent.VK_D:
                    return "D";
                case KeyEvent.VK_DEAD_ABOVEDOT:
                    return "DADOT";
                case KeyEvent.VK_DEAD_ABOVERING:
                    return "DARING";
                case KeyEvent.VK_DEAD_ACUTE:
                    return "DACUTE";
                case KeyEvent.VK_DEAD_BREVE:
                    return "DBREVE";
                case KeyEvent.VK_DEAD_CARON:
                    return "DCARON";
                case KeyEvent.VK_DEAD_CEDILLA:
                    return "DCEDLA";
                case KeyEvent.VK_DEAD_CIRCUMFLEX:
                    return "DCIRC";
                case KeyEvent.VK_DEAD_DIAERESIS:
                    return "DDIAER";
                case KeyEvent.VK_DEAD_DOUBLEACUTE:
                    return "DDACUT";
                case KeyEvent.VK_DEAD_GRAVE:
                    return "DGRAVE";
                case KeyEvent.VK_DEAD_IOTA:
                    return "DIOTA";
                case KeyEvent.VK_DEAD_MACRON:
                    return "DMACRON";
                case KeyEvent.VK_DEAD_OGONEK:
                    return "DOGNEK";
                case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
                    return "DSEMIV";
                case KeyEvent.VK_DEAD_TILDE:
                    return "D~";
                case KeyEvent.VK_DEAD_VOICED_SOUND:
                    return "DVOICE";
                case KeyEvent.VK_DECIMAL:
                    return "NUM.";
                case KeyEvent.VK_DELETE:
                    return "DEL";
                case KeyEvent.VK_DIVIDE:
                    return "NUM/";
                case KeyEvent.VK_DOLLAR:
                    return "$";
                case KeyEvent.VK_DOWN:
                    return "DOWN";
                case KeyEvent.VK_E:
                    return "E";
                case KeyEvent.VK_END:
                    return "END";
                case KeyEvent.VK_ENTER:
                    return "ENTER";
                case KeyEvent.VK_EQUALS:
                    return "=";
                case KeyEvent.VK_ESCAPE:
                    return "ESC";
                case KeyEvent.VK_EURO_SIGN:
                    return "EURO";
                case KeyEvent.VK_EXCLAMATION_MARK:
                    return "!";
                case KeyEvent.VK_F:
                    return "F";
                case KeyEvent.VK_F1:
                    return "F1";
                case KeyEvent.VK_F10:
                    return "F10";
                case KeyEvent.VK_F11:
                    return "F11";
                case KeyEvent.VK_F12:
                    return "F12";
                case KeyEvent.VK_F13:
                    return "F13";
                case KeyEvent.VK_F14:
                    return "F14";
                case KeyEvent.VK_F15:
                    return "F15";
                case KeyEvent.VK_F16:
                    return "F16";
                case KeyEvent.VK_F17:
                    return "F17";
                case KeyEvent.VK_F18:
                    return "F18";
                case KeyEvent.VK_F19:
                    return "F19";
                case KeyEvent.VK_F2:
                    return "F2";
                case KeyEvent.VK_F20:
                    return "F20";
                case KeyEvent.VK_F21:
                    return "F21";
                case KeyEvent.VK_F22:
                    return "F22";
                case KeyEvent.VK_F23:
                    return "F23";
                case KeyEvent.VK_F24:
                    return "F24";
                case KeyEvent.VK_F3:
                    return "F3";
                case KeyEvent.VK_F4:
                    return "F4";
                case KeyEvent.VK_F5:
                    return "F5";
                case KeyEvent.VK_F6:
                    return "F6";
                case KeyEvent.VK_F7:
                    return "F7";
                case KeyEvent.VK_F8:
                    return "F8";
                case KeyEvent.VK_F9:
                    return "F9";
                case KeyEvent.VK_FINAL:
                    return "FINAL";
                case KeyEvent.VK_FIND:
                    return "FIND";
                case KeyEvent.VK_FULL_WIDTH:
                    return "FULWTH";
                case KeyEvent.VK_G:
                    return "G";
                case KeyEvent.VK_GREATER:
                    return ">";
                case KeyEvent.VK_H:
                    return "H";
                case KeyEvent.VK_HALF_WIDTH:
                    return "HAFWTH";
                case KeyEvent.VK_HELP:
                    return "HELP";
                case KeyEvent.VK_HIRAGANA:
                    return "HRGANA";
                case KeyEvent.VK_HOME:
                    return "HOME";
                case KeyEvent.VK_I:
                    return "I";
                case KeyEvent.VK_INPUT_METHOD_ON_OFF:
                    return "IMOF";
                case KeyEvent.VK_INSERT:
                    return "INS";
                case KeyEvent.VK_INVERTED_EXCLAMATION_MARK:
                    return "¡";
                case KeyEvent.VK_J:
                    return "J";
                case KeyEvent.VK_JAPANESE_HIRAGANA:
                    return "JAPHGN";
                case KeyEvent.VK_JAPANESE_KATAKANA:
                    return "JAPKKN";
                case KeyEvent.VK_JAPANESE_ROMAN:
                    return "JAPRMN";
                case KeyEvent.VK_K:
                    return "K";
                case KeyEvent.VK_KANA:
                    return "KANA";
                case KeyEvent.VK_KANA_LOCK:
                    return "KANLK";
                case KeyEvent.VK_KANJI:
                    return "KANJI";
                case KeyEvent.VK_KATAKANA:
                    return "KTKANA";
                case KeyEvent.VK_KP_DOWN:
                    return "KPDOWN";
                case KeyEvent.VK_KP_LEFT:
                    return "KPLEFT";
                case KeyEvent.VK_KP_RIGHT:
                    return "KPRGHT";
                case KeyEvent.VK_KP_UP:
                    return "KPUP";
                case KeyEvent.VK_L:
                    return "L";
                case KeyEvent.VK_LEFT:
                    return "LEFT";
                case KeyEvent.VK_LEFT_PARENTHESIS:
                    return "(";
                case KeyEvent.VK_LESS:
                    return "<";
                case KeyEvent.VK_M:
                    return "M";
                case KeyEvent.VK_META:
                    return "META";
                case KeyEvent.VK_MINUS:
                    return "-";
                case KeyEvent.VK_MODECHANGE:
                    return "MODECH";
                case KeyEvent.VK_MULTIPLY:
                    return "NUM*";
                case KeyEvent.VK_N:
                    return "N";
                case KeyEvent.VK_NONCONVERT:
                    return "NONCNV";
                case KeyEvent.VK_NUM_LOCK:
                    return "NUMLK";
                case KeyEvent.VK_NUMBER_SIGN:
                    return "#";
                case KeyEvent.VK_NUMPAD0:
                    return "NUM0";
                case KeyEvent.VK_NUMPAD1:
                    return "NUM1";
                case KeyEvent.VK_NUMPAD2:
                    return "NUM2";
                case KeyEvent.VK_NUMPAD3:
                    return "NUM3";
                case KeyEvent.VK_NUMPAD4:
                    return "NUM4";
                case KeyEvent.VK_NUMPAD5:
                    return "NUM5";
                case KeyEvent.VK_NUMPAD6:
                    return "NUM6";
                case KeyEvent.VK_NUMPAD7:
                    return "NUM7";
                case KeyEvent.VK_NUMPAD8:
                    return "NUM8";
                case KeyEvent.VK_NUMPAD9:
                    return "NUM9";
                case KeyEvent.VK_O:
                    return "O";
                case KeyEvent.VK_OPEN_BRACKET:
                    return "]";
                case KeyEvent.VK_P:
                    return "P";
                case KeyEvent.VK_PAGE_DOWN:
                    return "PGDOWN";
                case KeyEvent.VK_PAGE_UP:
                    return "PGUP";
                case KeyEvent.VK_PASTE:
                    return "PASTE";
                case KeyEvent.VK_PAUSE:
                    return "PAUSE";
                case KeyEvent.VK_PERIOD:
                    return ".";
                case KeyEvent.VK_PLUS:
                    return "+";
                case KeyEvent.VK_PREVIOUS_CANDIDATE:
                    return "PRECAN";
                case KeyEvent.VK_PRINTSCREEN:
                    return "PRINT";
                case KeyEvent.VK_PROPS:
                    return "PROPS";
                case KeyEvent.VK_Q:
                    return "Q";
                case KeyEvent.VK_QUOTE:
                    return "'";
                case KeyEvent.VK_QUOTEDBL:
                    return "\"";
                case KeyEvent.VK_R:
                    return "R";
                case KeyEvent.VK_RIGHT:
                    return "RIGHT";
                case KeyEvent.VK_RIGHT_PARENTHESIS:
                    return ")";
                case KeyEvent.VK_ROMAN_CHARACTERS:
                    return "ROMAN";
                case KeyEvent.VK_S:
                    return "S";
                case KeyEvent.VK_SCROLL_LOCK:
                    return "SCROLK";
                case KeyEvent.VK_SEMICOLON:
                    return ";";
                case KeyEvent.VK_SEPARATOR:
                    return "SEPRAT";
                case KeyEvent.VK_SHIFT:
                    return "SHIFT";
                case KeyEvent.VK_SLASH:
                    return "/";
                case KeyEvent.VK_SPACE:
                    return "SPACE";
                case KeyEvent.VK_STOP:
                    return "STOP";
                case KeyEvent.VK_SUBTRACT:
                    return "NUM-";
                case KeyEvent.VK_T:
                    return "T";
                case KeyEvent.VK_TAB:
                    return "TAB";
                case KeyEvent.VK_U:
                    return "U";
                case KeyEvent.VK_UNDEFINED:
                    return "UNKNWN";
                case KeyEvent.VK_UNDERSCORE:
                    return "_";
                case KeyEvent.VK_UNDO:
                    return "UNDO";
                case KeyEvent.VK_UP:
                    return "UP";
                case KeyEvent.VK_V:
                    return "V";
                case KeyEvent.VK_W:
                    return "W";
                case KeyEvent.VK_WINDOWS:
                    return "WIN";
                case KeyEvent.VK_X:
                    return "X";
                case KeyEvent.VK_Y:
                    return "Y";
                case KeyEvent.VK_Z:
                    return "Z";
            }
        } else if (inputType == TYPE_MOUSE){
            switch (inputCode){
                case MouseEvent.BUTTON1:
                    return "LEFT CLICK";
                case MouseEvent.BUTTON2:
                    return "MIDDLE CLICK";
                case MouseEvent.BUTTON3:
                    return "RIGHT CLICK";
                case 4:
                    return "MOUSE4";
                case 5:
                    return "MOUES5";
                case 6:
                    return "MOUES6";
            }
        }
        return "UNKNOWN";
    }
}
