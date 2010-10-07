/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.remotecontrol

import grails.plugin.remotecontrol.client.*

class RemoteControl {

	static defaultReceiverAddress
	
	final classLoader
	final receiverAddress
	private final commandGenerator
	
	RemoteControl(String receiverAddress, ClassLoader classLoader) {
		this.receiverAddress = receiverAddress
		this.classLoader = classLoader
		this.commandGenerator = new CommandGenerator(classLoader)
	}
	
	def execute(Closure command) {
		def result = sendCommand(generateCommand(command))
		
		if (result.wasNull) {
			null
		} else if (result.wasUnserializable) {
			throw new Exception("Result was unserialisable [$result.stringRepresentation]")
		} else if (result.wasThrown) {
			throw new Exception("exception was thrown", result.value)
		} else {
			result.value
		}
	}

	static exec(Closure command) {
		if (!defaultReceiverAddress) {
			throw new IllegalStateException("Cannot use exec() as defaultReceiverAddress is not set")
		}

		// The contextClassLoader is set for each test type in GrailsTestTypeSupport
		def classLoader = Thread.currentThread().contextClassLoader
		
		new RemoteControl(defaultReceiverAddress, classLoader).execute(command)
	}
	
	protected Command generateCommand(Closure command) {
		commandGenerator.generate(command)
	}
	
	protected Result sendCommand(Command command) {
		new Sender(receiverAddress, classLoader).send(command)
	}

}