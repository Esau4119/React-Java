package codegen;

import constrain.*;
import visitor.*;
import java.util.*;
import ast.*;

/**
 *  The Frame class is used for tracking the frame size as we generated 
 *  bytecodes; we need the frame size to determine offsets for variables
*/
class Frame {
    private int size = 0;     // size of current frame
    private Stack<Block> blockSizes = new Stack<Block>();
       // If we are embedded 3 blocks deep in the current frame
       // then the blockSizes stack will have 3 items in it,
       // each recording the size of the associated block
       // e.g. consider the following source program segment
       //     int f(int n,int p) {  <- bottom block has formals
       //        int i;             <- next block has i 
       //        { int k; int l; int m <- next block has k, l, and m
       //         ...
       //        the blockSizes stack has 2,1,3 with 3 at the top
       //        the framesize is 6 - the sum of all the block sizes

    public Frame() {
        openBlock();
    }

    int getSize() {
        return size;
    }

    void openBlock() {
        blockSizes.push(new Block());
    }

    int closeBlock() {
        int bsize = getBlockSize();
        size -= bsize; // all items in the current block are gone
                       // so decrease the frame size
        blockSizes.pop();
        return bsize;
    }


    Block topBlock() {
        return (Block)blockSizes.peek();
    }

    void change(int n) {   // change current block size; framesize
        size += n;
        topBlock().change(n);
    }

    int getBlockSize() {
        return topBlock().getSize();
    }
}

/**
 *  The Block class is used to record the size of the current block
 *  Used in tandem with Frame
*/
class Block {
    int size = 0;

    void change(int n) {
        size += n;
    }

    int getSize() {
        return size;
    }
}

/**
 *  The Codegen class will walk the AST, determine and set variable
 *  offsets and generate the bytecodes
*/
public class Codegen extends ASTVisitor {

    AST t;
    Stack<Frame> frameSizes;   // used for tracking the frame sizes;
                        // when we start generating code for a
                        // function we'll push a new entry on the
                        // stack with init size zero
   
    Program program;    // program will contain the generated bytecodes
    int labelNum;       // used for creating new, unique labels

/**
 *  Create a new code generator based on the given AST
 *  @param t is the AST that will be visited
*/
    public Codegen(AST t) {
        this.t = t;
        program = new Program();
        frameSizes = new Stack<>();
        labelNum = 0;
    }

/** visit all the nodes in the AST/gen bytecodes
*/
    public Program execute() {
        t.accept(this);
        return program;
    }
        

    Frame topFrame() {
        if (frameSizes.empty())
        System.out.println("frames empty");
        return (Frame)frameSizes.peek();
    }

    void openFrame() {  // open a new frame - we're generating codes for
                        // a function declaration
        frameSizes.push(new Frame());
    }

    void openBlock() {  // open a new block - store local variables
        topFrame().openBlock();
    }

    void closeBlock() {  // close the current block (and pop the local
                         // variables off the runtime stack
        topFrame().closeBlock();
    }

    void closeFrame() {
        frameSizes.pop();
    }

    void changeFrame(int n) { // change the size of the current frame by the 
                              // given amount
        topFrame().change(n);
    }

    int frameSize() {  // return the current frame size
        return topFrame().getSize();
    }

    int getBlockSize() {
        return topFrame().getBlockSize();
    }

/** <pre>
 *  we'll need to create new labels for the bytecode program
 *  e.g. the following is legal despite 2 functions with the same name
 *        int f(int n) {... {int f() {... x = f()} } ... y = f(5)}
 *  we don't want to generate the label f for the start of both functions
 *  instead, we'll generate, e.g., f<<0>> and f<<1>>
 *  </pre>
*/
    String newLabel(String label) {  // create a new label from label
        ++labelNum;
        return label + "<<" + labelNum + ">>";
    }

    void storeop(Code code) {
/*        
        System.out.println("storeop: "+code.toString()+" fs:"+
           top.getSize()+" bs: "+top.getBlockSize());
*/           
        Codes.ByteCodes bytecode = code.getBytecode();
        int change = Codes.frameChange.get(bytecode);
        program.storeop(code);
        if (change == Codes.UnknownChange) {  // pop n; args n
            changeFrame( - ((NumOpcode)code).getNum());
        } else {
            changeFrame(change);
        }
    }

    void genIntrinsicCodes() {
        // generate codes for read/write functions so they're treated
        // as any other function
        String readLabel = "Read",
               writeLabel = "Write";
        AST readTree = Constrainer.readTree,
            writeTree = Constrainer.writeTree;
        readTree.setLabel(readLabel);
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,readLabel));
        storeop(new Code(Codes.ByteCodes.READ));
        storeop(new Code(Codes.ByteCodes.RETURN));

        writeTree.setLabel(writeLabel);
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,writeLabel));
        String formal = ((IdTree)(writeTree.getKid(3).getKid(1).getKid(2))).
                                 getSymbol().toString();
        storeop(new VarOpcode(Codes.ByteCodes.LOAD,0,formal));
        // write has one actual arg - in frame offset 0
        storeop(new Code(Codes.ByteCodes.WRITE));
        storeop(new Code(Codes.ByteCodes.RETURN));
   }

/** <pre>
 *  visit the given program AST:<br><br>
 *
 *  GOTO start   -- branch around codes for the intrinsics
 *  &LT;generate codes for the intrinsic trees (read/write)&GT;
 *  LABEL start
 *  &LT;generate codes for the BLOCK tree&GT;
 *  HALT
 *  </pre>
 *  @param t the program tree to visit
 *  @return null - we're a visitor so must return a value
 *  but the code generator doesn't need any specific value
*/
    public Object visitProgramTree(AST t) {
        String startLabel = newLabel("start");
        openFrame();
        storeop(new LabelOpcode(Codes.ByteCodes.GOTO,startLabel));
        // branch over intrinsic bytecodes
        genIntrinsicCodes();
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,startLabel));
        t.getKid(1).accept(this);
        storeop(new Code(Codes.ByteCodes.HALT));
        closeFrame();
        return null;
    }

/** <pre>
 *  Generate codes for the Block tree:<br><br>
 *  &LT;codes for the decls and the statements in the block&GT;
 *  POP n   -- n is the number of local variables; pop them
 *  </pre>
*/
    public Object visitBlockTree(AST t) {
        //System.out.println("visitBlockTree");
        openBlock();
        visitKids(t);
        storeop(new NumOpcode(Codes.ByteCodes.POP,getBlockSize()));
        // remove any local variables from runtime stack
        closeBlock();
        return null; }

/** <pre>
 *  Generate codes for the function declaration; we'll also record
 *  the frame offsets for the formal parameters<br><br>
 *  GOTO continue   -- branch around codes for the function
 *  LABEL functionLabel
 *  &LT;generate codes for the function body&GT;
 *  LIT 0
 *  RETURN function
 *  LABEL continue
 *  </pre>
*/
    public Object visitFunctionDeclTree(AST t) {
        //System.out.println("visitFunctionDeclTree");
        AST name = t.getKid(2),
            formals = t.getKid(3),
            block = t.getKid(4);
        String funcName = ((IdTree)name).getSymbol().toString();
        String funcLabel = newLabel(funcName);
        t.setLabel(funcLabel);
        String continueLabel = newLabel("continue");
        storeop(new LabelOpcode(Codes.ByteCodes.GOTO,continueLabel));
        openFrame();  // track Frame changes within function
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,funcLabel));
        // now record the frame offsets for the formals
        for (AST decl : formals.getKids()) {
            IdTree id = (IdTree)(decl.getKid(2));
            id.setFrameOffset(frameSize());
            decl.setLabel(id.getSymbol().toString());
            changeFrame(1);  // ensure frame size includes space for variables
        }
        block.accept(this);
        // emit gratis return in case user didn't provide her/his own return
        storeop(new VarOpcode(Codes.ByteCodes.LIT,0,"   GRATIS-RETURN-VALUE"));
        storeop(new LabelOpcode(Codes.ByteCodes.RETURN,funcLabel));
        closeFrame();
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,continueLabel));
        return null;
    }

/** <pre>
 *  Generate codes for the call tree:<br><br>
 *
 *  &LT;Codes to evaluate the arguments for the function&GT;
 *  ARGS <i>n</i>    where <i>n</i> is the number of args
 *  CALL functionName
 *  </pre>
*/
    public Object visitCallTree(AST t) {
        //System.out.println("visitCallTree");
        String funcName = ((IdTree)t.getKid(1)).getDecoration().getLabel();
        int numArgs = t.kidCount() - 1;
        for (int kid = 2; kid <= t.kidCount(); kid++) {
            t.getKid(kid).accept(this);
        }
        storeop(new NumOpcode(Codes.ByteCodes.ARGS,numArgs));
        //used to set up new frame
        storeop(new LabelOpcode(Codes.ByteCodes.CALL, funcName));
        return null;
    }

/** <pre>
 *  Generate codes for the Decl tree:<br><br>
 *
 *  LIT 0  -- 0 is the initial value for the variable
 *  record the frame offset of this variable for future references
 *  </pre>
*/
    public Object visitDeclTree(AST t) {
        //System.out.println("visitDeclTree");
        IdTree id = (IdTree)t.getKid(2);
        String idLabel = id.getSymbol().toString();
        t.setLabel(idLabel);  //set label in dcln node
        id.setFrameOffset(frameSize());
        storeop(new VarOpcode(Codes.ByteCodes.LIT,0,idLabel));
        //reserve space in frame for new variable; init to 0
        return null;
    }

    public Object visitIntTypeTree(AST t) {
        //System.out.println("visitIntTypeTree");
        return null; }

    public Object visitBoolTypeTree(AST t) {
        //System.out.println("visitBoolTypeTree");
        return null; }

    public Object visitFormalsTree(AST t) {
        //System.out.println("visitFormalsTree");
        return null; }

    public Object visitActualArgsTree(AST t) {
        //System.out.println("visitActualArgsTree");
        return null; }

/** <pre>
 *  Generate codes for the <i>If</i> tree:<br><br>
 *
 *  &LT;generate codes for the conditional tree&GT;
 *  FALSEBRANCH elseLabel
 *  &LT;generate codes for the <i>then</i> tree - 2nd kid&GT;
 *  GOTO continue
 *  LABEL elseLabel
 *  &LT;generate codes for the <i>else</i> tree - 3rd kid&GT;
 *  LABEL continue
 *  </pre>
*/
    public Object visitIfTree(AST t) {
        //System.out.println("visitIfTree");
        String elseLabel = newLabel("else"),
               continueLabel = newLabel("continue");
        t.getKid(1).accept(this); // gen code for conditional expr
        storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH,elseLabel));
        t.getKid(2).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.GOTO,continueLabel));
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,elseLabel));
        t.getKid(3).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,continueLabel));
        return null; }

/** <pre>
 *  Generate codes for the While tree:<br><br>
 *
 *  LABEL while
 *  &LT;generate codes for the conditional&GT;
 *  FALSEBRANCH continue
 *  &LT;generate codes for the body of the while&GT;
 *  GOTO while
 *  LABEL continue
 *  </pre>
*/
    public Object visitWhileTree(AST t) {
        //System.out.println("visitWhileTree");
        String continueLabel = newLabel("continue"),
               whileLabel = newLabel("while");
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,whileLabel));
        t.getKid(1).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.FALSEBRANCH,continueLabel));
        t.getKid(2).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.GOTO,whileLabel));
        storeop(new LabelOpcode(Codes.ByteCodes.LABEL,continueLabel));
        return null;
    }

/** <pre>
 *  Generate codes for the return tree:<br><br>
 *
 *  &LT;generate codes for the expression that will be returned&GT;
 *  RETURN &LT;name-of-function&GT;
 *  </pre>
*/
    public Object visitReturnTree(AST t) {
        //System.out.println("visitReturnTree");
        t.getKid(1).accept(this);
        AST fct = t.getDecoration();
        storeop(new LabelOpcode(Codes.ByteCodes.RETURN,fct.getLabel()));
        return null;
    }

/** <pre>
 *  Generate codes for the Assign tree:<br><br>
 *
 *  &LT;generate codes for the right-hand-side expression&GT;
 *  STORE <i>offset-of-variable name-of-variable</i>
 *  </pre>
*/
    public Object visitAssignTree(AST t) {
        //System.out.println("visitAssignTree");
        IdTree id = (IdTree)t.getKid(1);
        String vname = id.getSymbol().toString();
        int addr = ((IdTree)(id.getDecoration().getKid(2))).getFrameOffset();
        t.getKid(2).accept(this);
        storeop(new VarOpcode(Codes.ByteCodes.STORE,addr,vname));
        return null;
    }

/** <pre>
 *  Load a literal value:
 *  LIT <i>n</i>  n is the value 
 *  </pre>
*/
    public Object visitIntTree(AST t) {
        //System.out.println("visitIntTree");
        int num = Integer.parseInt(
                           ((IntTree)t).getSymbol().toString());
        storeop(new NumOpcode(Codes.ByteCodes.LIT,num));
        return null;
    }

/** <pre>
 *  Load a variable:
 *  LOAD <i>offset</i>  -- load variable using the offset recorded in the AST
 *  </pre>
*/
    public Object visitIdTree(AST t) {
        //System.out.println("visitIdTree");
        AST decl = t.getDecoration();
        int addr = ((IdTree)(decl.getKid(2))).getFrameOffset();
        String vname = ((IdTree)t).getSymbol().toString();
        storeop(new VarOpcode(Codes.ByteCodes.LOAD,addr,vname));
        return null;
    }

/** <pre>
 *  Generate codes for the relational op tree e.g. t1 == t2<br><br>
 *
 *  &LT;generate codes for t1&GT;
 *  &LT;generate codes for t2&GT;
 *  BOP op    -- op is the indicated relational op
 *  </pre>
*/
    public Object visitRelOpTree(AST t) {
        //System.out.println("visitRelOpTree");
        String op = ((RelOpTree)t).getSymbol().toString();
        t.getKid(1).accept(this);
        t.getKid(2).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.BOP,op));
        return null;
    }

/** <pre>
 *  Generate codes for the adding op tree e.g. t1 + t2<br><br>
 *
 *  &LT;generate codes for t1&GT;
 *  &LT;generate codes for t2&GT;
 *  BOP op    -- op is the indicated adding op
 *  </pre>
*/
    public Object visitAddOpTree(AST t) {
        //System.out.println("visitAddOpTree");
        String op = ((AddOpTree)t).getSymbol().toString();
        t.getKid(1).accept(this);
        t.getKid(2).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.BOP,op));
        return null;
    }

/** <pre>
 *  Generate codes for the multiplying op tree e.g. t1 * t2<br><br>
 *
 *  &LT;generate codes for t1&GT;
 *  &LT;generate codes for t2&GT;
 *  BOP op    -- op is the indicated multiplying op
 *  </pre>
*/
    public Object visitMultOpTree(AST t) {
        //System.out.println("visitMultOpTree");
        String op = ((MultOpTree)t).getSymbol().toString();
        t.getKid(1).accept(this);
        t.getKid(2).accept(this);
        storeop(new LabelOpcode(Codes.ByteCodes.BOP,op));
        return null;
    }
}