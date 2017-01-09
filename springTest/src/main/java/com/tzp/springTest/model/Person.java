package com.tzp.springTest.model;

public class Person {
	/**姓名**/
	private String name;
	/**年龄**/
	private int age;
	/**性别；0-男，1-女**/
	private String sex;
	/**  
	 * 获取姓名  
	 * @return name 姓名  
	 */
	public String getName() {
		return name;
	}
	/**  
	 * 设置姓名  
	 * @param name 姓名  
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**  
	 * 获取年龄  
	 * @return age 年龄  
	 */
	public int getAge() {
		return age;
	}
	/**  
	 * 设置年龄  
	 * @param age 年龄  
	 */
	public void setAge(int age) {
		this.age = age;
	}
	/**  
	 * 获取性别；0-男，1-女  
	 * @return sex 性别；0-男，1-女  
	 */
	public String getSex() {
		return sex;
	}
	/**  
	 * 设置性别；0-男，1-女  
	 * @param sex 性别；0-男，1-女  
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}
	@Override
	public String toString() {
		return String.format("姓名：%s，年龄：%s，性别：%s", this.name,this.age,this.sex);
	}
}
