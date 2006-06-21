/*
 * Copyright 2006 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: CimBase.java,v 1.1 2006-06-21 00:32:34 akhilarora Exp $
 */

package com.sun.ws.cim;

import com.sun.ws.cim.mapping.Namespaces;
import java.util.Vector;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMValue;

// TODO: convert to enum?
abstract class CimBase {
    
    public static final String CIM_UNSIGNED_BYTE = "cimUnsignedByte";
    public static final String CIM_BYTE = "cimByte";
    public static final String CIM_UNSIGNED_SHORT = "cimUnsignedShort";
    public static final String CIM_SHORT = "cimShort";
    public static final String CIM_UNSIGNED_INT = "cimUnsignedInt";
    public static final String CIM_INT = "cimInt";
    public static final String CIM_UNSIGNED_LONG = "cimUnsignedLong";
    public static final String CIM_LONG = "cimLong";
    public static final String CIM_STRING = "cimString";
    public static final String CIM_BOOLEAN = "cimBoolean";
    public static final String CIM_FLOAT = "cimFloat";
    public static final String CIM_DOUBLE = "cimDouble";
    public static final String CIM_DATE_TIME = "cimDatetime";
    public static final String CIM_CHAR16 = "cimChar16";
    public static final String CIM_REFERENCE = "cimReference";
    
    protected static final String ELEMENT = "element";
    protected static final String NAME= "name";
    protected static final String TYPE = "type";
    protected static final String MESSAGE = "message";
    protected static final String BASE = "base";
    protected static final String VALUE = "value";
    protected static final String _INPUT = "_INPUT";
    protected static final String _OUTPUT= "_OUTPUT";
    
    protected static final Vector EMPTY_VECTOR = new Vector(0);
    
    protected CimBase() {}
    
    protected String mapType(final int cimType) {
        
        String xsdType = null;
        
        switch (cimType) {
            case CIMDataType.UINT8:
            case CIMDataType.UINT8_ARRAY:
                xsdType = CIM_UNSIGNED_BYTE;
                break;
                
            case CIMDataType.SINT8:
            case CIMDataType.SINT8_ARRAY:
                xsdType = CIM_BYTE;
                break;
                
            case CIMDataType.UINT16:
            case CIMDataType.UINT16_ARRAY:
                xsdType = CIM_UNSIGNED_SHORT;
                break;
                
            case CIMDataType.SINT16:
            case CIMDataType.SINT16_ARRAY:
                xsdType = CIM_SHORT;
                break;
                
            case CIMDataType.UINT32:
            case CIMDataType.UINT32_ARRAY:
                xsdType = CIM_UNSIGNED_INT;
                break;
                
            case CIMDataType.SINT32:
            case CIMDataType.SINT32_ARRAY:
                xsdType = CIM_INT;
                break;
                
            case CIMDataType.UINT64:
            case CIMDataType.UINT64_ARRAY:
                xsdType = CIM_UNSIGNED_LONG;
                break;
                
            case CIMDataType.SINT64:
            case CIMDataType.SINT64_ARRAY:
                xsdType = CIM_LONG;
                break;
                
            case CIMDataType.STRING:
            case CIMDataType.STRING_ARRAY:
                xsdType = CIM_STRING;
                break;
                
            case CIMDataType.BOOLEAN:
            case CIMDataType.BOOLEAN_ARRAY:
                xsdType = CIM_BOOLEAN;
                break;
                
            case CIMDataType.REAL32:
            case CIMDataType.REAL32_ARRAY:
                xsdType = CIM_FLOAT;
                break;
                
            case CIMDataType.REAL64:
            case CIMDataType.REAL64_ARRAY:
                xsdType = CIM_DOUBLE;
                break;
                
            case CIMDataType.DATETIME:
            case CIMDataType.DATETIME_ARRAY:
                xsdType = CIM_DATE_TIME;
                break;
                
            case CIMDataType.CHAR16:
            case CIMDataType.CHAR16_ARRAY:
                xsdType = CIM_CHAR16;
                break;
                
            case CIMDataType.REFERENCE:
            case CIMDataType.REFERENCE_ARRAY:
                xsdType = CIM_REFERENCE;
                break;
                
            default:
                throw new IllegalArgumentException("Unable to map type: " + cimType);
        }
        return Namespaces.CIM_COMMON_NS_PREFIX + Namespaces.COLON + xsdType;
    }
    
    protected abstract Vector getCimQualifiers();
    protected abstract CIMDataType getCimType();
    protected abstract CIMValue getCimValue();
    protected abstract boolean hasCimQualifier(final String qualifier);
    protected abstract CIMQualifier getCimQualifier(final String qualifier);
    public abstract String getName();
    
    public CimQualifier[] getQualifiers() {
        final CimQualifier[] empty = {};
        final Vector quals = getCimQualifiers();
        if (quals != null) {
            final CimQualifier[] ret = new CimQualifier[quals.size()];
            for (int i = 0; i < ret.length; i++) {
                final Object qual = quals.get(i);
                if (qual instanceof CIMQualifier) {
                    final CIMQualifier cq = (CIMQualifier) qual;
                    ret[i] = new CimQualifier(cq);
                }
            }
            return ret;
        }
        return empty;
    }
    
    // returns negative if unbound
    public int getArraySize() {
        return getCimType().getSize();
    }
    
    public String getType() {
        return mapType(getCimType().getType());
    }
    
    public boolean isArrayType() {
        final CIMDataType type = getCimType();
        return type == null ? false : type.isArrayType();
    }
    
    public String getMaxLen() {
        return getQualifier("MaxLen");
    }
    
    public String getMaxValue() {
        return getQualifier("MaxValue");
    }
    
    public String getMinLen() {
        return getQualifier("MinLen");
    }
    
    public String getMinValue() {
        return getQualifier("MinValue");
    }
    
    public String getQualifier(final String qualifierName) {
        final CIMQualifier qual = getCimQualifier(qualifierName);
        return qual == null ? null : qual.getValue().toString();
    }
    
    public String[] getValueMap() {
        final String[] empty = {};
        final CIMQualifier qual = getCimQualifier("ValueMap");
        if (qual != null) {
            final CIMValue val = qual.getValue();
            if (val.getType().isArrayType()) {
                final Object obj = val.getValue();
                if (obj instanceof Vector) {
                    final Vector vector = (Vector) obj;
                    final String[] ret = new String[vector.size()];
                    for (int i = 0; i < ret.length; i++) {
                        final Object vm = vector.get(i);
                        if (vm instanceof String) {
                            ret[i] = (String) vm;
                        }
                    }
                    return ret;
                }
            }
        }
        return empty;
    }
    
    public boolean isEmbeddedInstance() {
        return hasCimQualifier("EmbeddedInstance");
    }
    
    public boolean isEmbeddedObject() {
        return hasCimQualifier("EmbeddedObject");
    }
    
    public boolean isRequired() {
        return hasCimQualifier("Required");
    }
    
    public boolean isOctetString() {
        return hasCimQualifier("OctetString");
    }

    public String getValue() {
        return getCimValue().toString();
    }
    
    public String[] getValues() {
        final String[] empty = {};
        final CIMValue values = getCimValue();
        if (values != null) {
            final Object obj = values.getValue();
            if (obj instanceof Vector) {
                final Vector vector = (Vector) obj;
                final String[] ret = new String[vector.size()];
                for (int i = 0; i < ret.length; i++) {
                    final Object vm = vector.get(i);
                    if (vm instanceof String) {
                        ret[i] = (String) vm;
                    }
                }
                return ret;
            }
        }
        return empty;
    }
}
