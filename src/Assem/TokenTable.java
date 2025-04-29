package Assem;

import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;
	LiteralTable litTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param symTab : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab, LiteralTable litTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		this.litTab = litTab;
		this.tokenList = new ArrayList<>();
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	 public void makeObjectCode(int index) {
		 	
		 Token t = getToken(index);
	        // 1) operator 없으면 스킵
	        if (t.operator == null) {
	            t.objectCode = "";
	            t.byteSize   = 0;
	            return;
	        }
	        // 2) 플래그 초기화
	        t.nixbpe = 0;
	        String op0 = (t.operand!=null && t.operand.length>0) ? t.operand[0] : null;
	        if (op0 != null && !op0.isEmpty()) {
	            char c0 = op0.charAt(0);
	            if (c0 == '@')      t.nixbpe |= nFlag;
	            else if (c0 == '#') t.nixbpe |= iFlag;
	            else                t.nixbpe |= (nFlag|iFlag);
	        }
	        if (t.operand!=null && t.operand.length>1 && "X".equals(t.operand[1])) {
	            t.nixbpe |= xFlag;
	        }
	        // 3) format4 체크
	        boolean is4 = t.operator.startsWith("+");
	        String mnem = is4 ? t.operator.substring(1) : t.operator;
	        if (is4) t.nixbpe |= eFlag;

	        // 4) Instruction 조회
	        Instruction inst = instTab.getInstruction(mnem);
	        if (inst == null) {
	            t.objectCode = "";
	            t.byteSize   = 0;
	            return;
	        }
	        int fmt    = is4 ? 4 : inst.format;
	        int opcode = inst.opcode;
	        int code = 0;

	        // 5-1) format2: 레지스터 두 개
	        if (fmt == 2) {
	            int r1 = (op0!=null) ? regNum(op0.charAt(0)) : 0;
	            int r2 = (t.operand.length>1 && t.operand[1]!=null) ? regNum(t.operand[1].charAt(0)) : 0;
	            code = (opcode << 8) | (r1 << 4) | r2;
	            t.byteSize = 2;
	        }
	        // 5-2) format3/4
	        else {
	            int disp = 0;
	            if (op0 != null) {
	                if (op0.startsWith("=")) {
	                    disp = litTab.searchLiteral(op0);
	                } else if (op0.startsWith("@")) {
	                    disp = symTab.searchSymbol(op0.substring(1));
	                } else if (op0.startsWith("#")) {
	                    disp = Integer.parseInt(op0.substring(1));
	                } else {
	                    // 기본 PC-relative
	                    int ta = symTab.searchSymbol(op0);
	                    int pc = t.location + (fmt==4 ? 4 : 3);
	                    int diff = ta - pc;
	                    if(ta == -1) {
	                    	disp = 0;
	                    }else {
	                    	 if (diff >= -2048 && diff < 2048) {
	 	                        t.nixbpe |= pFlag;
	 	                        disp = diff & 0xFFF;
	 	                    } else {
	 	                        t.nixbpe |= bFlag;
	 	                        disp = ta; // base-relative: 실제 base 레지스터는 여기에 미리 세팅돼 있어야 함
	 	                    }
	                    }
	                   
	                }
	            }
	            
	           
	            if (fmt == 3) {
	            	 code = ((opcode<<4) | (t.nixbpe) );
	                code = (code << 12) | (disp & 0xFFF);
	                t.byteSize = 3;
	            } else {
	            	 code = ((opcode<<4) | (nFlag|iFlag|eFlag) );
	                code = (code << 20) | (disp & 0xFFFFF);
	                t.byteSize = 4;
	            }
	        }

	        // 6) 16진수 문자열 생성
	        t.objectCode = String.format("%0" + (t.byteSize*2) + "X", code);
	 
	 }
	
	public int regNum(char c) {
	    switch (c) {
	        case 'A': return 0;
	        case 'X': return 1;
	        case 'B': return 3;
	        case 'S': return 4;
	        case 'T': return 5;
	        case 'F': return 6;
	        default:  return 0;
	    }
	}
	/** 
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	public int size() {
	    return tokenList.size();
	}
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
		this.location   = 0;
	    this.label      = "";
	    this.operator   = "";
	    this.operand    = new String[TokenTable.MAX_OPERAND];
	    for (int i = 0; i < this.operand.length; i++) {
	        this.operand[i] = null;
	    }
	    this.comment    = "";
	    this.nixbpe     = 0;
	    this.objectCode = "";
	    this.byteSize   = 0;
		parsing(line);
	}
	
	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		line = line.replaceAll("[\\r\\n]+$", "");
	    if (line.isEmpty() || line.startsWith(".")) {
	        // 주석이거나 빈 줄이면 모두 null 처리
	        label = operator = comment = null;
	        operand = new String[3];
	        return;
	    }
	    // 2) 공백(스페이스 또는 탭) 하나 이상으로 최대 4개로 분리
	    //    [0]=label, [1]=operator, [2]=operand 필드, [3]=comment(있으면)
	    String[] raw = line.split("\t", -1);

	    // 3) 무조건 4칸짜리 배열로 재구성
	    String[] parts = new String[4];
	    for (int i = 0; i < 4; i++) {
	        parts[i] = (i < raw.length) ? raw[i] : "";
	    }
	    // 3) label
	    label = parts[0].isEmpty() ? null : parts[0];

	    // 4) operator
	    operator = (parts.length > 1 && !parts[1].isEmpty()) ? parts[1] : null;

	    // 5) operand 배열 초기화 후, parts[2]가 있으면 ','로 분리
	    operand = new String[3];
	    if (parts.length > 2 && parts[2] != null) {
	        String[] ops = parts[2].split(",");
	        for (int i = 0; i < ops.length && i < 3; i++) {
	            operand[i] = ops[i].trim();
	        }
	    }

	    // 6) comment
	    if (parts.length > 3) {
	        comment = parts[3];
	    } else {
	        comment = null;
	    }
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. <br><br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
		if (value == 1) {
	        nixbpe |= flag;
	    } 
	}
	
	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br><br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
