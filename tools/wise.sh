# /*
#  * Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
#  *
#  * Licensed under the Apache License, Version 2.0 (the "License");
#  * you may not use this file except in compliance with the License.
#  * You may obtain a copy of the License at
#  *
#  *  http://www.apache.org/licenses/LICENSE-2.0
#  *
#  * Unless required by applicable law or agreed to in writing, software
#  * distributed under the License is distributed on an "AS IS" BASIS,
#  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  * See the License for the specific language governing permissions and
#  * limitations under the License.
#  *
#  *
#  ** Copyright (C) 2006, 2007 Hewlett-Packard Development Company, L.P.
#  **  
#  ** Authors: Simeon Pinder (simeon.pinder@hp.com), Denis Rachal (denis.rachal@hp.com), 
#  ** Nancy Beers (nancy.beers@hp.com), William Reichardt 
#  **
#  **$Log: not supported by cvs2svn $
#  **Revision 1.1  2007/05/31 20:18:28  nbeers
#  **Add HP copyright header.  Create new shell script for command line tool
#  **
#  ** 
#  *
#  * $Id: wise.sh,v 1.2 2007-06-01 18:48:43 nbeers Exp $
#  */

# set CLASSPATH
set CP=../lib/wiseman-core.jar;../lib/wiseman-tools.jar;../lib/jaxws/saaj-api.jar;../lib/jaxws/jaxb-api.jar;../lib/jaxws/saaj-impl.jar;../lib/jaxws/jaxb-impl.jar;../lib/jaxws/jsr173_api.jar

java -cp %CP% com.sun.ws.management.tools.WisemanCmdLine $@