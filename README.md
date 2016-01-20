# VoxMecanica Development Kit (Android)#
The VoxMecanica Development Kit for Android provides an open source API and runtime to create voice-enabled applications for mobile devices running the Android operating system.  With VoxMecanica, voice-enabled apps use a simple API to create voice-driven dialog style interactions.  Dialogs can be initiated locally in-app or from a remote source such as server, another connected device, or any networked source that can hosts an HTTP service end-point.  You simply describe your voice interactions a series of JSON graph or via the Java API.  The VDK accepts the dialog graph and create an interactive voice-enabled session between your application and the user.

## Features ##
* Supports for older devices (Android API Level-18)
* Uses Android's provided TTS and speech recognizer services
* Dialog interaction can be encoded as either a JSON or Java object graph
* The runtime automatically generates an interactive session from dialog graph
* An app can initiate dialogs from a remote HTTP source
* An app can also drive dialogs completely locally
* The VDK can be integrated as a simple JAR

## License ##
Apache License 2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.