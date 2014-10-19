package jp.gr.java_conf.utamaro.usobasic.client;

import java.util.HashMap;

public class BasicError extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private static HashMap<Integer,String> ERRORS=new HashMap<>();
	private int id;
	private static int MAX_ID=255;
	public final static int NEXT_WITHOUT_FOR=1;
	public final static int SYNTAX_ERROR=2;
	public final static int RETURN_WITHOUT_GOSUB=3;
	public final static int OUT_OF_DATA=4;
	public final static int ILLEGAL_FUNCTION_CALL=5;
	public final static int OVERFLOW=6;
	public final static int OUT_OF_MEMORY=7;
	public final static int UNDEFINED_LINE_NUMBER=8;
	public final static int SUBSCRIPT_OUT_OF_RANGE=9;
	public final static int DUPULICATE_DEFINITION=10;
	public final static int DIVISION_BY_ZERO=11;
	public final static int ILLEGAL_DIRECT=12;
	public final static int TYPE_MISMATCH=13;
	public final static int OUT_OF_STRING_SPACE=14;
	public final static int STRING_TOO_LONG=15;
	public final static int STRING_FORMULA_TOO_COMPLEX=16;
	public final static int CANT_CONTINUE=17;
	public final static int UNDEFINED_USER_FUNCTION=18;
	public final static int NO_RESUME=19;
	public final static int RESUME_WITHOUT_ERROR=20;
	public final static int MISSING_OPERAND=22;
	public final static int LINE_BUFFER_OVERFLOW=23;
	public final static int FOR_WITHOUT_NEXT=26;
	public final static int TAPE_READ_ERROR=27;
	public final static int WHILE_WITHOUT_WEND=29;
	public final static int WEND_WITHOUT_WHILE=30;
	public final static int DUPLICATE_LABEL=31;
	public final static int UNDEFINED_LABEL=32;
	public final static int FEATURE_NOT_AVAILABLE=33;
	public final static int FIELD_OVERFLOW=50;
	public final static int INTERNAL_ERROR=51;
	public final static int BAD_FILE_NUMBER=52;
	public final static int FILE_NOT_FOUND=53;
	public final static int FILE_ALREADY_OPEN=54;
	public final static int INPUT_PAST_END=55;
	public final static int BAD_FILE_NAME=56;
	public final static int DIRECT_STATEMENT_IN_FILE=57;
	public final static int SEQUENTIAL_I_O_ONLY=59;
	public final static int FILE_NOT_OPEN=60;
	public final static int FILE_WRITE_PROTECTED=61;
	public final static int DISK_OFFLINE=62;
	public final static int DISK_I_O_ERROR=64;
	public final static int FILE_ALREADY_EXIST=65;
	public final static int DISK_FULL=68;
	public final static int BAD_ALLOCATION_TABLE=69;
	public final static int BAD_DRIVE_NUMBER=70;
	public final static int BAD_TRACK_SECTOR=71;
	public final static int RENAME_ACROSS_DISKS=73;
	public final static int ILLEGAL_OPERATION=74;
	public final static int LINE_NUMBER_IS_ILLEGAL=254;
	public final static int BREAK=255;

	static{
		for(int i=0;i<MAX_ID;i++) ERRORS.put(i,"Unprintable error");
		ERRORS.put(NEXT_WITHOUT_FOR,"NEXT without FOR");
		ERRORS.put(SYNTAX_ERROR,"Syntax error");
		ERRORS.put(RETURN_WITHOUT_GOSUB,"RETURN without GOSUB");
		ERRORS.put(OUT_OF_DATA,"Out of DATA");
		ERRORS.put(ILLEGAL_FUNCTION_CALL,"Illegal function call");
		ERRORS.put(OVERFLOW,"Overflow");
		ERRORS.put(OUT_OF_MEMORY,"Out of memory");
		ERRORS.put(UNDEFINED_LINE_NUMBER,"Undefined line number");
		ERRORS.put(SUBSCRIPT_OUT_OF_RANGE,"Subscript out of range");
		ERRORS.put(DUPULICATE_DEFINITION,"Dupulicate Definition");
		ERRORS.put(DIVISION_BY_ZERO,"Division by Zero");
		ERRORS.put(ILLEGAL_DIRECT,"Illegal direct");
		ERRORS.put(TYPE_MISMATCH,"Type mismatch");
		ERRORS.put(OUT_OF_STRING_SPACE,"Out of string space");
		ERRORS.put(STRING_TOO_LONG,"String too long");
		ERRORS.put(STRING_FORMULA_TOO_COMPLEX,"String formula too complex");
		ERRORS.put(CANT_CONTINUE,"Can't continue");
		ERRORS.put(UNDEFINED_USER_FUNCTION,"Undefined user function");
		ERRORS.put(NO_RESUME,"No Resume");
		ERRORS.put(RESUME_WITHOUT_ERROR,"Resume without error");
		ERRORS.put(MISSING_OPERAND,"Missing operand");
		ERRORS.put(LINE_BUFFER_OVERFLOW,"Line buffer overflow");
		ERRORS.put(FOR_WITHOUT_NEXT,"FOR without NEXT");
		ERRORS.put(TAPE_READ_ERROR,"Tape read error");
		ERRORS.put(WHILE_WITHOUT_WEND,"WHILE without WEND");
		ERRORS.put(WEND_WITHOUT_WHILE,"WEND without WHILE");
		ERRORS.put(DUPLICATE_LABEL,"Duplicate Label");
		ERRORS.put(UNDEFINED_LABEL,"Undefined label");
		ERRORS.put(FEATURE_NOT_AVAILABLE,"Feature not available");
		ERRORS.put(FIELD_OVERFLOW,"FIELD overflow");
		ERRORS.put(INTERNAL_ERROR,"Internal error");
		ERRORS.put(BAD_FILE_NUMBER,"Bad file number");
		ERRORS.put(FILE_NOT_FOUND,"File not found");
		ERRORS.put(FILE_ALREADY_OPEN,"File already open");
		ERRORS.put(INPUT_PAST_END,"Input past end");
		ERRORS.put(BAD_FILE_NAME,"Bad file name");
		ERRORS.put(DIRECT_STATEMENT_IN_FILE,"Direct statement in file");
		ERRORS.put(SEQUENTIAL_I_O_ONLY,"Sequential I/O only");
		ERRORS.put(FILE_NOT_OPEN,"File not open");
		ERRORS.put(FILE_WRITE_PROTECTED,"File write protected");
		ERRORS.put(DISK_OFFLINE,"Disk offline");
		ERRORS.put(DISK_I_O_ERROR,"Disk I/O error");
		ERRORS.put(FILE_ALREADY_EXIST,"File already exist");
		ERRORS.put(DISK_FULL,"Disk full");
		ERRORS.put(BAD_ALLOCATION_TABLE,"Bad allocation table");
		ERRORS.put(BAD_DRIVE_NUMBER,"Bad drive number");
		ERRORS.put(BAD_TRACK_SECTOR,"Bad track / sector");
		ERRORS.put(RENAME_ACROSS_DISKS,"Rename across disks");
		ERRORS.put(ILLEGAL_OPERATION,"Illegal operation");
		ERRORS.put(LINE_NUMBER_IS_ILLEGAL,"Linenumber is not in ascending order ");
		ERRORS.put(BREAK,"Break");
	}

	
	public BasicError(int id){
		super(ERRORS.get(id));
		this.id=id;
	}

	public BasicError(int id,int line){
		super(ERRORS.get(id)+" in "+line);
		this.id=id;
	}
	
	public int getId(){
		return id;
	}
	public static void check(boolean ok,int id){
		if(!ok) throw new BasicError(id);
	}
}
