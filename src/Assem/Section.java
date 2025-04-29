package Assem;

import java.util.List;

public class Section {
    public String name;               // H 레코드: 프로그램 이름
    public int    startAddress;       // H 레코드: 시작 주소
    public int    length;             // H 레코드: 프로그램 길이

    public List<Define>      defines;     // D 레코드 항목
    public List<String>      references;  // R 레코드 항목
    public List<TextRecord>  textRecords; // T 레코드 항목
    public List<Modification> modifications; // M 레코드 항목

    public boolean isMain;           // 첫 섹션이면 true

    /** Define 항목을 담는 클래스 */
    public static class Define {
        public String symbol;
        public int    address;
    }

    /** Text 레코드를 담는 클래스 */
    public static class TextRecord {
        public int         startAddress;
        public int         length;        // 바이트 수
        public List<String> objectCodes;  // 예: ["141033","281030",...]
    }

    /** Modification 레코드를 담는 클래스 */
    public static class Modification {
        public int    address;
        public int    length;   // 바이트 수
        public String symbol;   // ex: "+BUFFER"
    }
}