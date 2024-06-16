package com.xuecheng.base.exception;


/**
 * @description 学成在线项目异常类
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
public class XueChengPlusException extends RuntimeException {

   private String message;

   private ResultEnum resultEnum;

   public XueChengPlusException() {
      super();
   }

   public XueChengPlusException(String message) {
      super(message);
      this.message = message;
   }

   public XueChengPlusException(ResultEnum resultEnum) {
      super(resultEnum.getMessage());
      this.resultEnum = resultEnum;
   }

   public String getErrMessage() {
      return message;
   }

   /*public static void cast(CommonError commonError){
       throw new XueChengPlusException(commonError.getErrMessage());
   }
   public static void cast(String errMessage){
       throw new XueChengPlusException(errMessage);
   }*/

}