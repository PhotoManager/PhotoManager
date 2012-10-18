#! /usr/bin/python
  


import string
import os 
import os.path
import sys
 
import time
 

src="."    
 
count = 0

def printLizenz(ff):
  
    ff.write("/*\n")
    ff.write(" * photo-manager is a program to manage and organize your photos; Copyright (C) 2010 Dietrich Hentschel\n")
    ff.write(" *\n")
    ff.write(" * This program is free software; you can redistribute it and/or modify it under the terms of the\n") 
    ff.write(" * GNU General Public License as published by the Free Software Foundation;\n") 
    ff.write(" * either version 2 of the License, or (at your option) any later version.\n")
    ff.write(" *\n")
    ff.write(" * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;\n") 
    ff.write(" * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n") 
    ff.write(" * See the GNU General Public License for more details.\n")
    ff.write(" *\n") 
    ff.write(" * You should have received a copy of the GNU General Public License along with this program;\n") 
    ff.write(" * if not, write to the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.\n")
    ff.write(" */\n")
   
         


def prog(path):
    f = open(path)
    l = f.readlines()
    f.close()
    f = open(path,"w")
    printLizenz(f)
    for line in l:
        f.write(line)
    f.close()
    
    

def insert(x, dirname, files):
    global count
    for name in files:
        if name.endswith(".java"):
            count = count + 1
            path = os.path.join(dirname, name)
            print "datei = ", path
            prog(path)
          
       #     sys.exit(0)
      
      
def insertText():
    os.path.walk(src , insert, None)

 
 
  
  
 
insertText()
 

print "anzahl Programme = " , count

###  Ende  #######################
print "end program"    
