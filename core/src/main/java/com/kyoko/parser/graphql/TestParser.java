package com.kyoko.parser.graphql;

import com.kyoko.common.CoreLog;

public class TestParser {
	public static void main(String args[]){
		CoreLog.log("TestParser");
		try {
//			Parser yyparser = new Parser("query abc { name } ");
//			Parser yyparser = new Parser("query abc { name (aaa: 123) } ");
//			Parser yyparser = new Parser("query abc { name (aaa: \"ABC\") } ");
//			Parser yyparser = new Parser("query abc ($f1:String = \"ABC\") { name (aaa: \"ABC\") } ");
//			Parser yyparser = new Parser("query abc ($f1:String = \"ABC\r\nDEF\r\n\") { name (aaa: \"ABC\") } ");
//			Parser yyparser = new Parser("query abc ($f1:String = \"\"\"ABC\r\nDEF\r\n\"\"\") { name (aaa: \"ABC\") } ");
			Parser yyparser = new Parser("query abc ($f1:String = \"ABC\r\nDEF\r\n\") { name (aaa: $f1) } ");
//			Parser yyparser = new Parser(" mutation aaa { likeStory(storyID: 12345) { story { likeCount } } }");
					
					
//			Parser yyparser = new Parser("123");
			Object result = yyparser.parse();
			CoreLog.log(result.toString());
		} catch (Exception ex){ 
			CoreLog.log(ex);
		}
	}
}
