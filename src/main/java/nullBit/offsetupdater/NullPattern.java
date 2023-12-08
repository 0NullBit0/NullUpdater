package nullBit.offsetupdater;



import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.Math;


//user
import nullBit.offsetupdater.Utils;
import capstone.Capstone;

public class NullPattern
{
    private String aarch;
    private Capstone cs;
    

    
    
    public NullPattern(String arch){
        this.aarch = arch;
        switch(aarch){
            case "arm": 
                cs = new Capstone(Capstone.CS_ARCH_ARM, Capstone.CS_MODE_ARM); break;
            case "arm64":
                cs = new Capstone(Capstone.CS_ARCH_ARM64, Capstone.CS_MODE_ARM); break;
            default:
                cs = new Capstone(Capstone.CS_ARCH_ARM64, Capstone.CS_MODE_ARM);
                this.aarch = "arm64";
        }
    }
    
    public NullByte[] getPattern(byte[] data, int address, int mode){
    	int addition;
    	if(mode == 0)
    		addition = 0;
    	else if(mode > 0) {
    		addition = 0x4 * (int)Math.pow(2, mode+5);
    	}
    	else {
    		addition = 0x4 * mode;
    	}
        int endOfFuncAddr = getEOF(data, address) + addition;
        
        if(endOfFuncAddr > data.length || endOfFuncAddr < address)
        	return null;
        
        byte[] toEnd = new byte[endOfFuncAddr-address];
        for(int i = 0; i< toEnd.length; i++) 
            toEnd[i] = data[i + address];
        	
        
        Capstone.CsInsn[] insn = cs.disasm(toEnd, (long) address);
        NullByte[] pattern = new NullByte[toEnd.length];
        int kount = 0;
        switch(aarch){
            case "arm64":
                
                for(int i = 0; i<insn.length; i++){
                    if(insn[i].mnemonic.equals("ldr") || insn[i].mnemonic.equals("str")
                    && Utils.hasTwoComma(insn[i].opStr)){
                        for(int j = 0; j < 3; j++){
                            pattern[kount] = new NullByte((byte)-1, true);
                            kount++;
                        }
                        pattern[kount] = new NullByte(data[(int)insn[i].address + 0x3], false);
                        kount++;
                        continue;
                    }
                    
                    if(insn[i].mnemonic.equals("ldrb") || insn[i].mnemonic.equals("strb")
                        && Utils.hasTwoComma(insn[i].opStr)){
                        for(int j = 0; j < 3; j++){
                            pattern[kount] = new NullByte((byte)-1, true);
                            kount++;
                        }
                        pattern[kount] = new NullByte((byte)0x39, false);
                        kount++;
                        continue;
                    }
                    
                    if(insn[i].mnemonic.equals("add")){
                        for(int j = 0; j < 3; j++){
                            pattern[kount] = new NullByte((byte)-1, true);
                            kount++;
                        }
                        pattern[kount] = new NullByte(data[(int)insn[i].address + 0x3], false);
                        kount++;
                        continue;
                    }
                    
                    Pattern regEx = Pattern.compile("#0x[0-9a-fA-F]{4,}", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = regEx.matcher(insn[i].opStr);
                    
                    if(matcher.find()){
                        for(int j = 0; j < 4; j++){
                            pattern[kount] = new NullByte((byte)-1, true);
                            kount++;
                        } 
                        continue;
                    }
                    
                    //default 
                    for(int j = 0; j < 4; j++){
                        pattern[kount] = new NullByte(data[(int) insn[i].address + j], false);
                        kount++;
                    }
                }
                
                break;
        }
        
        
        
        if(pattern.length < 0 || pattern[0] == null) {
            System.out.println("[NULLLOG] WARNING PROBABLY INVALID LOCATION");
        	return null;
        }
        
        return pattern;
    }

    
    private int getEOF(byte[] data, int address){
        int lengthToEnd = data.length - address;
        byte[] toEnd = new byte[lengthToEnd];
        for(int i = 0; i<lengthToEnd; i++)
            toEnd[i] = data[i + address];
        
        Capstone.CsInsn[] insn = cs.disasm(toEnd, (long)address);
        
        for (int i =0; i<insn.length; i++){
            //System.out.println(insn[i].mnemonic);
        		
    		if( insn[i].mnemonic.equals("ret") ){
    			return (int)insn[i].address + 0x4;  
    		}
        		
        	
        }
        
        return -1;
    }
    
    public boolean testCapstone(){
        byte[] armCODE = {(byte)0x1,(byte)0x0,(byte)0xA0,(byte)0xE3};
        byte[] arm64CODE = {(byte)0x20,(byte)0x0,(byte)0x80,(byte)0xD2};
        
        switch(aarch){
            case "arm64":
                Capstone.CsInsn[] insnARMV8 = cs.disasm(arm64CODE, (long)0);
                return insnARMV8.length > 0;
            case "arm":
                Capstone.CsInsn[] insnARMV7 = cs.disasm(armCODE, (long)0);
                return insnARMV7.length > 0;
        }
        
        return false;
    }
}