/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.util.exceptions;

/**
 * Exception thrown when a ballerina native constructs fail to register.
 */
public class NativeException extends BallerinaException {
    
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@link NativeException} with the specified detail message.
     * 
     * @param message   Error Message
     */
    public NativeException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new {@link NativeException} with the specified detail message and cause.
     * 
     * @param message   Error message
     * @param cause     Cause
     */
    public NativeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link NativeException} with the cause.
     * 
     * @param cause Cause
     */
    public NativeException(Throwable cause) {
        super(cause);
    }
}
