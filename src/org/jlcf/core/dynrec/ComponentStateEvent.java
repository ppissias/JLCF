/**
 * Copyright 2013 Petros Pissias.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jlcf.core.dynrec;

/**
 * Enumeration defining possible events that are 
 * related to the external state of the component.
 * 
 * @author Petros Pissias
 *
 */
public enum ComponentStateEvent {
	CALLING, //method call starts
	FINISHED_CALLING, //method call ends
	RECONFIGURATION_START, //reconfiguration starts
	RECONFIGURATION_END //reconfiguration ends
}
