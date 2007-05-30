/*
 * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
 **  
 ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
 ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
 **
 **$Log: not supported by cvs2svn $
 ** 
 *
 * $Id: UserModelObject.java,v 1.3 2007-05-30 20:30:22 nbeers Exp $
 */
package framework.models;



public class UserModelObject {
	private String firstname;
	private String lastname;
//	private int age;
	private Integer age;
	private String Address;
	private String city;
	private String state;
	private String zip;
	private static final int HASH_PRIME = 1000003;

	public UserModelObject() {
		super();	
	}
	public String getAddress() {
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
//	public int getAge() {
	public Integer getAge() {
		return age;
	}
//	public void setAge(int age) {
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
	}
	
	@Override
	public boolean equals(Object obj) {
        /**
         * return true if they are the same object
         */
        if (this == obj)
            return true;
        /**
         * the following two tests only need to be performed
         * if this class is directly derived from java.lang.Object
         */
        if (obj == null || obj.getClass() != getClass())
            return false;
        UserModelObject otherUser = (UserModelObject)obj;
        if(otherUser.getLastname().equals(this.getLastname()))
            if(otherUser.getFirstname().equals(this.getFirstname()))
            	return true;
        return false;
	}
	@Override
	public int hashCode() {
        int result = 0;
        result = HASH_PRIME * result + lastname.hashCode();
        result = HASH_PRIME * result + firstname.hashCode();
		return result;
	}
	
	

}
