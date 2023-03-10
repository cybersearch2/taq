/* Generated By:JavaCC: Do not edit this line. TaqParserTokenManager.java */
package au.com.cybersearch2.taq.model;

/** Copyright 2022 Andrew J Bowley

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. */

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import au.com.cybersearch2.taq.language.BooleanTerm;
import au.com.cybersearch2.taq.language.DoubleTerm;
import au.com.cybersearch2.taq.language.DualIndex;
import au.com.cybersearch2.taq.language.ExpressionIndex;
import au.com.cybersearch2.taq.language.Group;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.IVariableSpec;
import au.com.cybersearch2.taq.language.IntegerTerm;
import au.com.cybersearch2.taq.language.KeyName;
import au.com.cybersearch2.taq.language.ListReference;
import au.com.cybersearch2.taq.language.LiteralParameter;
import au.com.cybersearch2.taq.language.LiteralType;
import au.com.cybersearch2.taq.language.QualifiedName;
import au.com.cybersearch2.taq.language.QuerySpec;
import au.com.cybersearch2.taq.language.StringTerm;
import au.com.cybersearch2.taq.language.TextDecoder;
import au.com.cybersearch2.taq.language.Unknown;
import au.com.cybersearch2.taq.language.TaqLiteral;
import au.com.cybersearch2.taq.language.Term;
import au.com.cybersearch2.taq.language.IOperand;
import au.com.cybersearch2.taq.language.ITemplate;
import au.com.cybersearch2.taq.language.OperandType;
import au.com.cybersearch2.taq.language.Parameter;
import au.com.cybersearch2.taq.language.InitialProperties;
import au.com.cybersearch2.taq.engine.Compiler;
import au.com.cybersearch2.taq.language.SyntaxException;
import au.com.cybersearch2.taq.engine.Extent;
import au.com.cybersearch2.taq.engine.Parser;
import au.com.cybersearch2.taq.engine.SourceItem;
import au.com.cybersearch2.taq.engine.SourceMarker;
import au.com.cybersearch2.taq.engine.SourceTracker;
import au.com.cybersearch2.taq.engine.Unit;
import au.com.cybersearch2.taq.artifact.NameIndex;
import au.com.cybersearch2.taq.artifact.NestedFlowArtifact;
import au.com.cybersearch2.taq.artifact.ArchetypeArtifact;
import au.com.cybersearch2.taq.artifact.TemplateArtifact;
import au.com.cybersearch2.taq.artifact.FunctionArtifact;
import au.com.cybersearch2.taq.artifact.ResourceArtifact;
import au.com.cybersearch2.taq.artifact.ListArtifact;
import au.com.cybersearch2.taq.artifact.ChoiceArtifact;
import au.com.cybersearch2.taq.artifact.TermArtifact;

/** Token Manager. */
public class TaqParserTokenManager implements TaqParserConstants
{

  /** Debug output. */
  public  java.io.PrintStream debugStream = System.out;
  /** Set debug output. */
  public  void setDebugStream(java.io.PrintStream ds) { debugStream = ds; }
private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1)
{
   switch (pos)
   {
      case 0:
         if ((active1 & 0x40080L) != 0L)
            return 0;
         if ((active0 & 0x4000000000000L) != 0L || (active1 & 0x4000000L) != 0L)
            return 10;
         if ((active0 & 0x1e03fffffc0L) != 0L)
         {
            jjmatchedKind = 41;
            return 24;
         }
         return -1;
      case 1:
         if ((active0 & 0x1e03fffffc0L) != 0L)
         {
            if (jjmatchedPos != 1)
            {
               jjmatchedKind = 41;
               jjmatchedPos = 1;
            }
            return 24;
         }
         return -1;
      case 2:
         if ((active0 & 0x10000100000L) != 0L)
            return 24;
         if ((active0 & 0xe03fefffc0L) != 0L)
         {
            if (jjmatchedPos != 2)
            {
               jjmatchedKind = 41;
               jjmatchedPos = 2;
            }
            return 24;
         }
         return -1;
      case 3:
         if ((active0 & 0x202008c000L) != 0L)
            return 24;
         if ((active0 & 0xc01fe73fc0L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 3;
            return 24;
         }
         return -1;
      case 4:
         if ((active0 & 0x4002400040L) != 0L)
            return 24;
         if ((active0 & 0x801da73f80L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 4;
            return 24;
         }
         return -1;
      case 5:
         if ((active0 & 0xc003400L) != 0L)
            return 24;
         if ((active0 & 0x8011a70b80L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 5;
            return 24;
         }
         return -1;
      case 6:
         if ((active0 & 0x8001260980L) != 0L)
            return 24;
         if ((active0 & 0x10810200L) != 0L)
         {
            jjmatchedKind = 41;
            jjmatchedPos = 6;
            return 24;
         }
         return -1;
      default :
         return -1;
   }
}
private final int jjStartNfa_0(int pos, long active0, long active1)
{
   return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
}
private int jjStopAtPos(int pos, int kind)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   return pos + 1;
}
private int jjMoveStringLiteralDfa0_0()
{
   switch(curChar)
   {
      case 33:
         jjmatchedKind = 57;
         return jjMoveStringLiteralDfa1_0(0x8000000000000000L, 0x0L);
      case 35:
         return jjStopAtPos(0, 92);
      case 36:
         return jjStopAtPos(0, 91);
      case 37:
         jjmatchedKind = 75;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x400000L);
      case 38:
         jjmatchedKind = 72;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x80002L);
      case 40:
         return jjStopAtPos(0, 42);
      case 41:
         return jjStopAtPos(0, 43);
      case 42:
         jjmatchedKind = 70;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x20000L);
      case 43:
         jjmatchedKind = 68;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x8004L);
      case 44:
         return jjStopAtPos(0, 49);
      case 45:
         jjmatchedKind = 69;
         return jjMoveStringLiteralDfa1_0(0x8000000000000L, 0x10008L);
      case 46:
         jjmatchedKind = 50;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x4000000L);
      case 47:
         jjmatchedKind = 71;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x40000L);
      case 58:
         return jjStopAtPos(0, 58);
      case 59:
         return jjStopAtPos(0, 48);
      case 60:
         jjmatchedKind = 55;
         return jjMoveStringLiteralDfa1_0(0x2000000000000000L, 0x801000L);
      case 61:
         jjmatchedKind = 54;
         return jjMoveStringLiteralDfa1_0(0x1000000000000000L, 0x0L);
      case 62:
         jjmatchedKind = 56;
         return jjMoveStringLiteralDfa1_0(0x4000000000000000L, 0x3006000L);
      case 63:
         return jjStopAtPos(0, 59);
      case 64:
         return jjStopAtPos(0, 52);
      case 78:
         return jjMoveStringLiteralDfa1_0(0x10000000000L, 0x0L);
      case 91:
         return jjStopAtPos(0, 46);
      case 93:
         return jjStopAtPos(0, 47);
      case 94:
         jjmatchedKind = 74;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x200000L);
      case 96:
         return jjStopAtPos(0, 53);
      case 97:
         return jjMoveStringLiteralDfa1_0(0x40L, 0x0L);
      case 98:
         return jjMoveStringLiteralDfa1_0(0x80L, 0x0L);
      case 99:
         return jjMoveStringLiteralDfa1_0(0x700L, 0x0L);
      case 100:
         return jjMoveStringLiteralDfa1_0(0x1800L, 0x0L);
      case 101:
         return jjMoveStringLiteralDfa1_0(0x2000L, 0x0L);
      case 102:
         return jjMoveStringLiteralDfa1_0(0x400001c000L, 0x0L);
      case 105:
         return jjMoveStringLiteralDfa1_0(0x60000L, 0x0L);
      case 108:
         return jjMoveStringLiteralDfa1_0(0x80000L, 0x0L);
      case 109:
         return jjMoveStringLiteralDfa1_0(0x100000L, 0x0L);
      case 112:
         return jjMoveStringLiteralDfa1_0(0x200000L, 0x0L);
      case 113:
         return jjMoveStringLiteralDfa1_0(0x400000L, 0x0L);
      case 114:
         return jjMoveStringLiteralDfa1_0(0x1800000L, 0x0L);
      case 115:
         return jjMoveStringLiteralDfa1_0(0xe000000L, 0x0L);
      case 116:
         return jjMoveStringLiteralDfa1_0(0x2030000000L, 0x0L);
      case 117:
         return jjMoveStringLiteralDfa1_0(0x8000000000L, 0x0L);
      case 123:
         return jjStopAtPos(0, 44);
      case 124:
         jjmatchedKind = 73;
         return jjMoveStringLiteralDfa1_0(0x0L, 0x100001L);
      case 125:
         return jjStopAtPos(0, 45);
      case 126:
         return jjStopAtPos(0, 93);
      default :
         return jjMoveNfa_0(5, 0);
   }
}
private int jjMoveStringLiteralDfa1_0(long active0, long active1)
{
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(0, active0, active1);
      return 1;
   }
   switch(curChar)
   {
      case 38:
         if ((active1 & 0x2L) != 0L)
            return jjStopAtPos(1, 65);
         break;
      case 43:
         if ((active1 & 0x4L) != 0L)
            return jjStopAtPos(1, 66);
         break;
      case 45:
         if ((active1 & 0x8L) != 0L)
            return jjStopAtPos(1, 67);
         break;
      case 46:
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x4000000L);
      case 60:
         if ((active1 & 0x1000L) != 0L)
         {
            jjmatchedKind = 76;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x800000L);
      case 61:
         if ((active0 & 0x1000000000000000L) != 0L)
            return jjStopAtPos(1, 60);
         else if ((active0 & 0x2000000000000000L) != 0L)
            return jjStopAtPos(1, 61);
         else if ((active0 & 0x4000000000000000L) != 0L)
            return jjStopAtPos(1, 62);
         else if ((active0 & 0x8000000000000000L) != 0L)
            return jjStopAtPos(1, 63);
         else if ((active1 & 0x8000L) != 0L)
            return jjStopAtPos(1, 79);
         else if ((active1 & 0x10000L) != 0L)
            return jjStopAtPos(1, 80);
         else if ((active1 & 0x20000L) != 0L)
            return jjStopAtPos(1, 81);
         else if ((active1 & 0x40000L) != 0L)
            return jjStopAtPos(1, 82);
         else if ((active1 & 0x80000L) != 0L)
            return jjStopAtPos(1, 83);
         else if ((active1 & 0x100000L) != 0L)
            return jjStopAtPos(1, 84);
         else if ((active1 & 0x200000L) != 0L)
            return jjStopAtPos(1, 85);
         else if ((active1 & 0x400000L) != 0L)
            return jjStopAtPos(1, 86);
         break;
      case 62:
         if ((active0 & 0x8000000000000L) != 0L)
            return jjStopAtPos(1, 51);
         else if ((active1 & 0x4000L) != 0L)
         {
            jjmatchedKind = 78;
            jjmatchedPos = 1;
         }
         return jjMoveStringLiteralDfa2_0(active0, 0L, active1, 0x3002000L);
      case 97:
         return jjMoveStringLiteralDfa2_0(active0, 0x14000304000L, active1, 0L);
      case 99:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa2_0(active0, 0x35800800L, active1, 0L);
      case 105:
         return jjMoveStringLiteralDfa2_0(active0, 0x80000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000L, active1, 0L);
      case 110:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000060000L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa2_0(active0, 0x1180L, active1, 0L);
      case 114:
         return jjMoveStringLiteralDfa2_0(active0, 0x2000000000L, active1, 0L);
      case 116:
         return jjMoveStringLiteralDfa2_0(active0, 0x8000000L, active1, 0L);
      case 117:
         return jjMoveStringLiteralDfa2_0(active0, 0x410600L, active1, 0L);
      case 120:
         return jjMoveStringLiteralDfa2_0(active0, 0x2040L, active1, 0L);
      case 124:
         if ((active1 & 0x1L) != 0L)
            return jjStopAtPos(1, 64);
         break;
      default :
         break;
   }
   return jjStartNfa_0(0, active0, active1);
}
private int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(0, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(1, active0, active1);
      return 2;
   }
   switch(curChar)
   {
      case 46:
         if ((active1 & 0x4000000L) != 0L)
            return jjStopAtPos(2, 90);
         break;
      case 61:
         if ((active1 & 0x800000L) != 0L)
            return jjStopAtPos(2, 87);
         else if ((active1 & 0x1000000L) != 0L)
            return jjStopAtPos(2, 88);
         break;
      case 62:
         if ((active1 & 0x2000L) != 0L)
         {
            jjmatchedKind = 77;
            jjmatchedPos = 2;
         }
         return jjMoveStringLiteralDfa3_0(active0, 0L, active1, 0x2000000L);
      case 78:
         if ((active0 & 0x10000000000L) != 0L)
            return jjStartNfaWithStates_0(2, 40, 24);
         break;
      case 99:
         return jjMoveStringLiteralDfa3_0(active0, 0x24800L, active1, 0L);
      case 101:
         return jjMoveStringLiteralDfa3_0(active0, 0x400000L, active1, 0L);
      case 105:
         return jjMoveStringLiteralDfa3_0(active0, 0x40L, active1, 0L);
      case 107:
         return jjMoveStringLiteralDfa3_0(active0, 0x8000000000L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa3_0(active0, 0x4004000000L, active1, 0L);
      case 109:
         return jjMoveStringLiteralDfa3_0(active0, 0x10000100L, active1, 0L);
      case 110:
         return jjMoveStringLiteralDfa3_0(active0, 0x10000L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa3_0(active0, 0x2008080L, active1, 0L);
      case 112:
         if ((active0 & 0x100000L) != 0L)
            return jjStartNfaWithStates_0(2, 20, 24);
         return jjMoveStringLiteralDfa3_0(active0, 0x2000L, active1, 0L);
      case 114:
         return jjMoveStringLiteralDfa3_0(active0, 0x28000600L, active1, 0L);
      case 115:
         return jjMoveStringLiteralDfa3_0(active0, 0x880000L, active1, 0L);
      case 116:
         return jjMoveStringLiteralDfa3_0(active0, 0x240000L, active1, 0L);
      case 117:
         return jjMoveStringLiteralDfa3_0(active0, 0x2000001000L, active1, 0L);
      case 118:
         return jjMoveStringLiteralDfa3_0(active0, 0x1000000L, active1, 0L);
      default :
         break;
   }
   return jjStartNfa_0(1, active0, active1);
}
private int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(1, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(2, active0, active1);
      return 3;
   }
   switch(curChar)
   {
      case 61:
         if ((active1 & 0x2000000L) != 0L)
            return jjStopAtPos(3, 89);
         break;
      case 98:
         return jjMoveStringLiteralDfa4_0(active0, 0x1000L, active1, 0L);
      case 99:
         return jjMoveStringLiteralDfa4_0(active0, 0x10000L, active1, 0L);
      case 101:
         if ((active0 & 0x2000000000L) != 0L)
            return jjStartNfaWithStates_0(3, 37, 24);
         return jjMoveStringLiteralDfa4_0(active0, 0x5040000L, active1, 0L);
      case 105:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000800L, active1, 0L);
      case 108:
         return jjMoveStringLiteralDfa4_0(active0, 0x20080L, active1, 0L);
      case 109:
         if ((active0 & 0x20000000L) != 0L)
            return jjStartNfaWithStates_0(3, 29, 24);
         break;
      case 110:
         return jjMoveStringLiteralDfa4_0(active0, 0x8000000000L, active1, 0L);
      case 111:
         return jjMoveStringLiteralDfa4_0(active0, 0x802040L, active1, 0L);
      case 112:
         return jjMoveStringLiteralDfa4_0(active0, 0x12000100L, active1, 0L);
      case 114:
         return jjMoveStringLiteralDfa4_0(active0, 0x400200L, active1, 0L);
      case 115:
         return jjMoveStringLiteralDfa4_0(active0, 0x4000000400L, active1, 0L);
      case 116:
         if ((active0 & 0x4000L) != 0L)
            return jjStartNfaWithStates_0(3, 14, 24);
         else if ((active0 & 0x80000L) != 0L)
            return jjStartNfaWithStates_0(3, 19, 24);
         return jjMoveStringLiteralDfa4_0(active0, 0x200000L, active1, 0L);
      case 119:
         if ((active0 & 0x8000L) != 0L)
            return jjStartNfaWithStates_0(3, 15, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(2, active0, active1);
}
private int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1)
{
   if (((active0 &= old0) | (active1 &= old1)) == 0L)
      return jjStartNfa_0(2, old0, old1);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(3, active0, 0L);
      return 4;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa5_0(active0, 0x4000000L);
      case 101:
         if ((active0 & 0x2000000L) != 0L)
            return jjStartNfaWithStates_0(4, 25, 24);
         else if ((active0 & 0x4000000000L) != 0L)
            return jjStartNfaWithStates_0(4, 38, 24);
         return jjMoveStringLiteralDfa5_0(active0, 0x200280L);
      case 103:
         return jjMoveStringLiteralDfa5_0(active0, 0x40000L);
      case 108:
         return jjMoveStringLiteralDfa5_0(active0, 0x10001100L);
      case 109:
         if ((active0 & 0x40L) != 0L)
            return jjStartNfaWithStates_0(4, 6, 24);
         return jjMoveStringLiteralDfa5_0(active0, 0x800L);
      case 110:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000000L);
      case 111:
         return jjMoveStringLiteralDfa5_0(active0, 0x8000000400L);
      case 114:
         return jjMoveStringLiteralDfa5_0(active0, 0x1002000L);
      case 116:
         return jjMoveStringLiteralDfa5_0(active0, 0x10000L);
      case 117:
         return jjMoveStringLiteralDfa5_0(active0, 0x820000L);
      case 121:
         if ((active0 & 0x400000L) != 0L)
            return jjStartNfaWithStates_0(4, 22, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(3, active0, 0L);
}
private int jjMoveStringLiteralDfa5_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(3, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(4, active0, 0L);
      return 5;
   }
   switch(curChar)
   {
      case 97:
         return jjMoveStringLiteralDfa6_0(active0, 0x10000880L);
      case 100:
         return jjMoveStringLiteralDfa6_0(active0, 0x20000L);
      case 101:
         if ((active0 & 0x1000L) != 0L)
            return jjStartNfaWithStates_0(5, 12, 24);
         return jjMoveStringLiteralDfa6_0(active0, 0x40100L);
      case 103:
         if ((active0 & 0x8000000L) != 0L)
            return jjStartNfaWithStates_0(5, 27, 24);
         break;
      case 105:
         return jjMoveStringLiteralDfa6_0(active0, 0x10000L);
      case 110:
         return jjMoveStringLiteralDfa6_0(active0, 0x200L);
      case 114:
         if ((active0 & 0x400L) != 0L)
            return jjStartNfaWithStates_0(5, 10, 24);
         return jjMoveStringLiteralDfa6_0(active0, 0xa00000L);
      case 115:
         return jjMoveStringLiteralDfa6_0(active0, 0x1000000L);
      case 116:
         if ((active0 & 0x2000L) != 0L)
            return jjStartNfaWithStates_0(5, 13, 24);
         else if ((active0 & 0x4000000L) != 0L)
            return jjStartNfaWithStates_0(5, 26, 24);
         break;
      case 119:
         return jjMoveStringLiteralDfa6_0(active0, 0x8000000000L);
      default :
         break;
   }
   return jjStartNfa_0(4, active0, 0L);
}
private int jjMoveStringLiteralDfa6_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(4, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(5, active0, 0L);
      return 6;
   }
   switch(curChar)
   {
      case 99:
         return jjMoveStringLiteralDfa7_0(active0, 0x800200L);
      case 101:
         if ((active0 & 0x20000L) != 0L)
            return jjStartNfaWithStates_0(6, 17, 24);
         else if ((active0 & 0x1000000L) != 0L)
            return jjStartNfaWithStates_0(6, 24, 24);
         break;
      case 108:
         if ((active0 & 0x800L) != 0L)
            return jjStartNfaWithStates_0(6, 11, 24);
         break;
      case 110:
         if ((active0 & 0x80L) != 0L)
            return jjStartNfaWithStates_0(6, 7, 24);
         else if ((active0 & 0x200000L) != 0L)
            return jjStartNfaWithStates_0(6, 21, 24);
         else if ((active0 & 0x8000000000L) != 0L)
            return jjStartNfaWithStates_0(6, 39, 24);
         break;
      case 111:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000L);
      case 114:
         if ((active0 & 0x40000L) != 0L)
            return jjStartNfaWithStates_0(6, 18, 24);
         break;
      case 116:
         return jjMoveStringLiteralDfa7_0(active0, 0x10000000L);
      case 120:
         if ((active0 & 0x100L) != 0L)
            return jjStartNfaWithStates_0(6, 8, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(5, active0, 0L);
}
private int jjMoveStringLiteralDfa7_0(long old0, long active0)
{
   if (((active0 &= old0)) == 0L)
      return jjStartNfa_0(5, old0, 0L);
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) {
      jjStopStringLiteralDfa_0(6, active0, 0L);
      return 7;
   }
   switch(curChar)
   {
      case 101:
         if ((active0 & 0x800000L) != 0L)
            return jjStartNfaWithStates_0(7, 23, 24);
         else if ((active0 & 0x10000000L) != 0L)
            return jjStartNfaWithStates_0(7, 28, 24);
         break;
      case 110:
         if ((active0 & 0x10000L) != 0L)
            return jjStartNfaWithStates_0(7, 16, 24);
         break;
      case 121:
         if ((active0 & 0x200L) != 0L)
            return jjStartNfaWithStates_0(7, 9, 24);
         break;
      default :
         break;
   }
   return jjStartNfa_0(6, active0, 0L);
}
private int jjStartNfaWithStates_0(int pos, int kind, int state)
{
   jjmatchedKind = kind;
   jjmatchedPos = pos;
   try { curChar = input_stream.readChar(); }
   catch(java.io.IOException e) { return pos + 1; }
   return jjMoveNfa_0(state, pos + 1);
}
static final long[] jjbitVec0 = {
   0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL
};
private int jjMoveNfa_0(int startState, int curPos)
{
   int startsAt = 0;
   jjnewStateCnt = 37;
   int i = 1;
   jjstateSet[0] = startState;
   int kind = 0x7fffffff;
   for (;;)
   {
      if (++jjround == 0x7fffffff)
         ReInitRounds();
      if (curChar < 64)
      {
         long l = 1L << curChar;
         do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((0x3ff000000000000L & l) != 0L)
                  {
                     if (kind > 30)
                        kind = 30;
                     jjCheckNAddStates(0, 5);
                  }
                  else if (curChar == 39)
                     jjCheckNAddTwoStates(21, 22);
                  else if (curChar == 34)
                     jjCheckNAddStates(6, 8);
                  else if (curChar == 46)
                     jjCheckNAdd(10);
                  else if (curChar == 47)
                     jjstateSet[jjnewStateCnt++] = 0;
                  if (curChar == 48)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 0:
                  if (curChar == 47)
                     jjCheckNAddStates(9, 11);
                  break;
               case 1:
                  if ((0xffffffffffffdbffL & l) != 0L)
                     jjCheckNAddStates(9, 11);
                  break;
               case 2:
                  if ((0x2400L & l) != 0L && kind > 5)
                     kind = 5;
                  break;
               case 3:
                  if (curChar == 10 && kind > 5)
                     kind = 5;
                  break;
               case 4:
                  if (curChar == 13)
                     jjstateSet[jjnewStateCnt++] = 3;
                  break;
               case 6:
                  if (curChar == 48)
                     jjstateSet[jjnewStateCnt++] = 7;
                  break;
               case 8:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 30)
                     kind = 30;
                  jjstateSet[jjnewStateCnt++] = 8;
                  break;
               case 9:
                  if (curChar == 46)
                     jjCheckNAdd(10);
                  break;
               case 10:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddStates(12, 14);
                  break;
               case 12:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(13);
                  break;
               case 13:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddTwoStates(13, 14);
                  break;
               case 15:
                  if (curChar == 34)
                     jjCheckNAddStates(6, 8);
                  break;
               case 16:
                  if ((0xfffffffbffffdbffL & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 18:
                  if ((0x408400000000L & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 19:
                  if (curChar == 34 && kind > 35)
                     kind = 35;
                  break;
               case 20:
                  if (curChar == 39)
                     jjCheckNAddTwoStates(21, 22);
                  break;
               case 21:
                  if ((0xffffff7fffffdbffL & l) != 0L)
                     jjCheckNAddTwoStates(21, 22);
                  break;
               case 22:
                  if (curChar == 39 && kind > 36)
                     kind = 36;
                  break;
               case 24:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 41)
                     kind = 41;
                  jjstateSet[jjnewStateCnt++] = 24;
                  break;
               case 25:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 30)
                     kind = 30;
                  jjCheckNAddStates(0, 5);
                  break;
               case 26:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 30)
                     kind = 30;
                  jjCheckNAdd(26);
                  break;
               case 27:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddTwoStates(27, 28);
                  break;
               case 28:
                  if (curChar != 46)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddStates(15, 17);
                  break;
               case 29:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddStates(15, 17);
                  break;
               case 31:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(32);
                  break;
               case 32:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAddTwoStates(32, 14);
                  break;
               case 33:
                  if ((0x3ff000000000000L & l) != 0L)
                     jjCheckNAddStates(18, 20);
                  break;
               case 35:
                  if ((0x280000000000L & l) != 0L)
                     jjCheckNAdd(36);
                  break;
               case 36:
                  if ((0x3ff000000000000L & l) == 0L)
                     break;
                  if (kind > 33)
                     kind = 33;
                  jjCheckNAdd(36);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else if (curChar < 128)
      {
         long l = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 5:
                  if ((0x7fffffe07fffffeL & l) == 0L)
                     break;
                  if (kind > 41)
                     kind = 41;
                  jjCheckNAdd(24);
                  break;
               case 1:
                  jjAddStates(9, 11);
                  break;
               case 7:
                  if ((0x100000001000000L & l) != 0L)
                     jjCheckNAdd(8);
                  break;
               case 8:
                  if ((0x7e0000007eL & l) == 0L)
                     break;
                  if (kind > 30)
                     kind = 30;
                  jjCheckNAdd(8);
                  break;
               case 11:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(21, 22);
                  break;
               case 14:
                  if ((0x1000000010L & l) != 0L && kind > 33)
                     kind = 33;
                  break;
               case 16:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 17:
                  if (curChar == 92)
                     jjstateSet[jjnewStateCnt++] = 18;
                  break;
               case 18:
                  if ((0x14404410000000L & l) != 0L)
                     jjCheckNAddStates(6, 8);
                  break;
               case 21:
                  if ((0xffffffffefffffffL & l) != 0L)
                     jjAddStates(23, 24);
                  break;
               case 24:
                  if ((0x7fffffe87fffffeL & l) == 0L)
                     break;
                  if (kind > 41)
                     kind = 41;
                  jjCheckNAdd(24);
                  break;
               case 30:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(25, 26);
                  break;
               case 34:
                  if ((0x2000000020L & l) != 0L)
                     jjAddStates(27, 28);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      else
      {
         int i2 = (curChar & 0xff) >> 6;
         long l2 = 1L << (curChar & 077);
         do
         {
            switch(jjstateSet[--i])
            {
               case 1:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(9, 11);
                  break;
               case 16:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(6, 8);
                  break;
               case 21:
                  if ((jjbitVec0[i2] & l2) != 0L)
                     jjAddStates(23, 24);
                  break;
               default : break;
            }
         } while(i != startsAt);
      }
      if (kind != 0x7fffffff)
      {
         jjmatchedKind = kind;
         jjmatchedPos = curPos;
         kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 37 - (jjnewStateCnt = startsAt)))
         return curPos;
      try { curChar = input_stream.readChar(); }
      catch(java.io.IOException e) { return curPos; }
   }
}
static final int[] jjnextStates = {
   26, 27, 28, 33, 34, 14, 16, 17, 19, 1, 2, 4, 10, 11, 14, 29, 
   30, 14, 33, 34, 14, 12, 13, 21, 22, 31, 32, 35, 36, 
};

/** Token literal values. */
public static final String[] jjstrLiteralImages = {
"", null, null, null, null, null, "\141\170\151\157\155", 
"\142\157\157\154\145\141\156", "\143\157\155\160\154\145\170", "\143\165\162\162\145\156\143\171", 
"\143\165\162\163\157\162", "\144\145\143\151\155\141\154", "\144\157\165\142\154\145", 
"\145\170\160\157\162\164", "\146\141\143\164", "\146\154\157\167", "\146\165\156\143\164\151\157\156", 
"\151\156\143\154\165\144\145", "\151\156\164\145\147\145\162", "\154\151\163\164", "\155\141\160", 
"\160\141\164\164\145\162\156", "\161\165\145\162\171", "\162\145\163\157\165\162\143\145", 
"\162\145\166\145\162\163\145", "\163\143\157\160\145", "\163\145\154\145\143\164", 
"\163\164\162\151\156\147", "\164\145\155\160\154\141\164\145", "\164\145\162\155", null, null, null, null, 
null, null, null, "\164\162\165\145", "\146\141\154\163\145", 
"\165\156\153\156\157\167\156", "\116\141\116", null, "\50", "\51", "\173", "\175", "\133", "\135", "\73", 
"\54", "\56", "\55\76", "\100", "\140", "\75", "\74", "\76", "\41", "\72", "\77", 
"\75\75", "\74\75", "\76\75", "\41\75", "\174\174", "\46\46", "\53\53", "\55\55", "\53", 
"\55", "\52", "\57", "\46", "\174", "\136", "\45", "\74\74", "\76\76\76", "\76\76", 
"\53\75", "\55\75", "\52\75", "\57\75", "\46\75", "\174\75", "\136\75", "\45\75", 
"\74\74\75", "\76\76\75", "\76\76\76\75", "\56\56\56", "\44", "\43", "\176", };

/** Lexer state names. */
public static final String[] lexStateNames = {
   "DEFAULT",
};
static final long[] jjtoToken = {
   0xfffffffa7fffffc1L, 0x3fffffffL, 
};
static final long[] jjtoSkip = {
   0x3eL, 0x0L, 
};
protected SimpleCharStream input_stream;
private final int[] jjrounds = new int[37];
private final int[] jjstateSet = new int[74];
protected char curChar;
/** Constructor. */
public TaqParserTokenManager(SimpleCharStream stream){
   if (SimpleCharStream.staticFlag)
      throw new Error("ERROR: Cannot use a static CharStream class with a non-static lexical analyzer.");
   input_stream = stream;
}

/** Constructor. */
public TaqParserTokenManager(SimpleCharStream stream, int lexState){
   this(stream);
   SwitchTo(lexState);
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream)
{
   jjmatchedPos = jjnewStateCnt = 0;
   curLexState = defaultLexState;
   input_stream = stream;
   ReInitRounds();
}
private void ReInitRounds()
{
   int i;
   jjround = 0x80000001;
   for (i = 37; i-- > 0;)
      jjrounds[i] = 0x80000000;
}

/** Reinitialise parser. */
public void ReInit(SimpleCharStream stream, int lexState)
{
   ReInit(stream);
   SwitchTo(lexState);
}

/** Switch to specified lex state. */
public void SwitchTo(int lexState)
{
   if (lexState >= 1 || lexState < 0)
      throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", TokenMgrError.INVALID_LEXICAL_STATE);
   else
      curLexState = lexState;
}

protected Token jjFillToken()
{
   final Token t;
   final String curTokenImage;
   final int beginLine;
   final int endLine;
   final int beginColumn;
   final int endColumn;
   String im = jjstrLiteralImages[jjmatchedKind];
   curTokenImage = (im == null) ? input_stream.GetImage() : im;
   beginLine = input_stream.getBeginLine();
   beginColumn = input_stream.getBeginColumn();
   endLine = input_stream.getEndLine();
   endColumn = input_stream.getEndColumn();
   t = Token.newToken(jjmatchedKind, curTokenImage);

   t.beginLine = beginLine;
   t.endLine = endLine;
   t.beginColumn = beginColumn;
   t.endColumn = endColumn;

   return t;
}

int curLexState = 0;
int defaultLexState = 0;
int jjnewStateCnt;
int jjround;
int jjmatchedPos;
int jjmatchedKind;

/** Get the next Token. */
public Token getNextToken() 
{
  Token matchedToken;
  int curPos = 0;

  EOFLoop :
  for (;;)
  {
   try
   {
      curChar = input_stream.BeginToken();
   }
   catch(java.io.IOException e)
   {
      jjmatchedKind = 0;
      matchedToken = jjFillToken();
      return matchedToken;
   }

   try { input_stream.backup(0);
      while (curChar <= 32 && (0x100002600L & (1L << curChar)) != 0L)
         curChar = input_stream.BeginToken();
   }
   catch (java.io.IOException e1) { continue EOFLoop; }
   jjmatchedKind = 0x7fffffff;
   jjmatchedPos = 0;
   curPos = jjMoveStringLiteralDfa0_0();
   if (jjmatchedKind != 0x7fffffff)
   {
      if (jjmatchedPos + 1 < curPos)
         input_stream.backup(curPos - jjmatchedPos - 1);
      if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L)
      {
         matchedToken = jjFillToken();
         return matchedToken;
      }
      else
      {
         continue EOFLoop;
      }
   }
   int error_line = input_stream.getEndLine();
   int error_column = input_stream.getEndColumn();
   String error_after = null;
   boolean EOFSeen = false;
   try { input_stream.readChar(); input_stream.backup(1); }
   catch (java.io.IOException e1) {
      EOFSeen = true;
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
      if (curChar == '\n' || curChar == '\r') {
         error_line++;
         error_column = 0;
      }
      else
         error_column++;
   }
   if (!EOFSeen) {
      input_stream.backup(1);
      error_after = curPos <= 1 ? "" : input_stream.GetImage();
   }
   throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, TokenMgrError.LEXICAL_ERROR);
  }
}

private void jjCheckNAdd(int state)
{
   if (jjrounds[state] != jjround)
   {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
   }
}
private void jjAddStates(int start, int end)
{
   do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
   } while (start++ != end);
}
private void jjCheckNAddTwoStates(int state1, int state2)
{
   jjCheckNAdd(state1);
   jjCheckNAdd(state2);
}

private void jjCheckNAddStates(int start, int end)
{
   do {
      jjCheckNAdd(jjnextStates[start]);
   } while (start++ != end);
}

}
