import java.util.*;
public class ThreeAddrCode implements CALParserVisitor {

    private static int labelCounter = 1;
    private static int nestedLevel = 0;
    private static Hashtable<String, Integer> labels  = new Hashtable<String, Integer>();
    private static int tCounter = 1;
    private static int paramCount = 1;

    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("Visit SimpleNode");
    }

    public Object visit(ASTProgram node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTDeclaration_List node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTVariable_Declaration node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTConstant_Declaration node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTIdentifier node, Object data){
        
        return node.value;
    }

    public Object visit(ASTNumber node, Object data){
        if(Integer.parseInt((String)node.value) < 0) {
            if((node.jjtGetParent()).toString().equals("Assignment")) {
                return node.value;
            }
            String temp = "t" + tCounter;
            tCounter++;

            System.out.println("        " + temp + " = " + node.value);
            return temp;
        }
        return node.value;
    }

    public Object visit(ASTFunction_List node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTFunction node, Object data){
        SimpleNode id = (SimpleNode) node.jjtGetChild(1);
        System.out.println((String) id.value + ":");
        node.childrenAccept(this, data);
        paramCount = 1;
        return "function";
    }

    public Object visit(ASTFunctionReturn node, Object data){
        if(node.jjtGetNumChildren() != 0) {
            String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
            System.out.println("\treturn " + child1);
            node.childrenAccept(this, data);    
        }
        
        return data;
    }

    public Object visit(ASTType node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTParameter_List node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTParameter node, Object data){
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        System.out.println("\t" + child1 + " = getparam " + paramCount);
        paramCount++;
        node.jjtGetChild(1).jjtAccept(this, data);
        return data;
    }

    public Object visit(ASTMain node, Object data){
        System.out.println("main:");
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTStatement_Block node, Object data){
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTAssignment node, Object data){
        
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String child2 = (String) node.jjtGetChild(1).jjtAccept(this, data);
        String s = child1 + " " + "=" + " " + child2;
        System.out.println("        " + s );
       
        return data;
    }

    public Object visit(ASTStatement node, Object data){
        if(((String)node.value).equals("if")){
            return ifStatement(node, data);
        }
        else if(((String)node.value).equals("while")){
            return whileStatement(node, data);
        }
        else{ //handles else
            node.jjtGetChild(0).jjtAccept(this, data);
            return data;
        }

        
    }

    public Object visit(ASTSkip node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTMinus_Operator node, Object data){
        return tempCreator(node, data);
    }

    public Object visit(ASTPlus_Operator node, Object data){
        return tempCreator(node, data);

    }

    public Object visit(ASTAnd_Operator node, Object data){
        return tempCreator(node, data);
    }

    public Object visit(ASTOr_Operator node, Object data){
        return tempCreator(node, data);
    }


    public Object visit(ASTNegative node, Object data){
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        if((node.jjtGetParent()).toString().equals("Assignment")) {
            String strForAssignmnet = "-" + child1;
            return strForAssignmnet;
        }
        String minus = "-" + child1;
        String temp = "t" + tCounter;
        tCounter++;

        System.out.println("        " + temp + " = " + minus);
        return temp;
        
    }

    public Object visit(ASTBoolean node, Object data){
        String child1 = (String) node.value;
        
        return child1;
    }

    public Object visit(ASTFunctionCall node, Object data){
        int countParams = 0;
        countParams = getParams((ASTArg_List) node.jjtGetChild(1), data, countParams);
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String funcCall = child1 + " " + countParams; 
        String functionParent = ((node.jjtGetParent()).toString());    
        if(functionParent.equals("Statement_Block")) {
            System.out.println("        " + funcCall);    
        } 
        
        return funcCall;
    }

    public Object visit(ASTEqOP node, Object data){
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String child2 = (String) node.jjtGetChild(1).jjtAccept(this, data);

        String temp = "t" + tCounter;
        tCounter++;

        System.out.println("        " + temp + " = " + child1 + " " + " == " + " " + child2 ); //Not using node.value for equality
        return temp;
    }

    public Object visit(ASTNotEqOP node, Object data){
       return tempCreator(node, data);
    }

    public Object visit(ASTLessThanOP node, Object data){
        return tempCreator(node, data);
    }

    public Object visit(ASTLessOrEqualOP node, Object data){
        return tempCreator(node, data);
    }

    public Object visit(ASTGreaterThanOP node, Object data){
        return tempCreator(node, data);
    }

    public Object visit(ASTGreaterOrEqualOP node, Object data){
        return tempCreator(node, data);
        
    }

   
    public Object visit(ASTArg_List node, Object data){
        
        node.childrenAccept(this, data);
        return data;
    }

    private String whileStatement(SimpleNode node, Object data) {
        System.out.println("L" + labelCounter + ":");
        labelCounter++;
        nestedLevel++;
        labels.put("while" + nestedLevel, labelCounter);
        node.jjtGetChild(0).jjtAccept(this, data);
        int tmpCount = tCounter -1;
        System.out.println("\tifz t" + tmpCount + " goto L" + labelCounter);
        labelCounter++;
        node.jjtGetChild(1).jjtAccept(this, data);
        if(labels.get("while" + nestedLevel) != null){
            int tempLabel = labels.get("while" + nestedLevel) - 1;
            System.out.println("\tgoto L" + tempLabel);
            System.out.println("L" + labels.get("while" + nestedLevel) + ":");
            
        }
        nestedLevel--;
        return "while";
    }

    private String ifStatement(SimpleNode node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        nestedLevel++;
        int tmpCount = tCounter -1;
        System.out.println("\tifz t" + tmpCount + " goto L" + labelCounter);
        labelCounter++;
        labels.put("if" + nestedLevel, labelCounter);
        node.jjtGetChild(1).jjtAccept(this, data);
        System.out.println("\tgoto L" + labelCounter);
        int tempLabel = labelCounter -1;
        labelCounter++;
        System.out.println("L" + tempLabel + ":");
        node.jjtGetChild(2).jjtAccept(this, data);
        if(labels.get("if" + nestedLevel)!=null){
            System.out.println("L"+ labels.get("if" + nestedLevel) + ":");
        }
        nestedLevel--;


        return "if";
    }

    private Object tempCreator(SimpleNode node, Object data) {
        String child1 = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String child2 = (String) node.jjtGetChild(1).jjtAccept(this, data);
        if((node.jjtGetParent()).toString().equals("Assignment")) {
            String strForAssignmnet = child1 + " " + node.value + " " +  child2;
            return strForAssignmnet;
        }

        String temp = "t" + tCounter;
        tCounter++;

        System.out.println("        " + temp + " = " + child1 + "  " + node.value + "  " + child2 );
        return temp;
    }


    private int getParams(ASTArg_List node, Object data, int count){
        if(node.jjtGetNumChildren() != 0) {
            System.out.println("\tparam " + node.jjtGetChild(0).jjtAccept(this, data));
            return getParams((ASTArg_List)node.jjtGetChild(1), data, ++count);
        }
        return count;  
    }

}