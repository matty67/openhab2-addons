# Lupus XT2 Binding

## Supported Things

Show state of Lupus XT2 

lupus:xt2:7b2410cf [hostname="<lupus_hostname>", user="<lupus_user>", pass="<lupus_password>", port=10508 ]

## Discovery

## Binding Configuration

No binding config needed.

```
# Configuration for the Lupus Binding
#
Add a normal user to your XT2 and edit the entries <lupus_hostname>, <lupus_user> and <lupus_password> in your things file.
Port as default is 10508.

Then in your XT2 under Settings/Contact ID add this String.

ip://<4digit number>@<ip of your openhab2>:<port>/CID
e.g .
ip://2222@192.168.0.70:10508/CID


## Thing Configuration


## Channels
MODE
Number mode_a1
Number mode_a2

Values (put this in lupus.map undertransform)
0=Disarmed
1=Armed
2=Homemodus 1
3=Homemodus 2
4=Homemodus 3


STATE
Number Lupus_State1 
Number Lupus_State2 

Eventcodes (look in your XT2 manual)
eg. 
130 is Burglary (Einbruchalarm)

If you compare the value of Lupus_State1 in a rule, you should use hex values.
e.g.
    if (Lupus_State1.state != NULL && Lupus_State1.state>=0x130 && Lupus_State1.state<=0x136) { 
        postUpdate(Shutter_All, DOWN)
    }

MESSAGE
String Lupus_State1_Msg 
String Lupus_State2_Msg 

Text of eventcode (look in your XT2 manual)

## Full Example

#file lupus.things
lupus:xt2:7b2410cf [hostname="192.168.0.222", user="openhab2", pass="blabla", port=10508 ]

#file lupus.items
Number Lupus_Mode1 "Zone 1[MAP(lupus.map):%s]" <lupus_shield> {channel="lupus:xt2:7b2410cf:mode_a1"}
Number Lupus_Mode2 "Zone 2[MAP(lupus.map):%s]" <lupus_shield> {channel="lupus:xt2:7b2410cf:mode_a2"}
Number Lupus_State1 {channel="lupus:xt2:7b2410cf:state_a1"}
Number Lupus_State2 {channel="lupus:xt2:7b2410cf:state_a2"}
String Lupus_State1_Msg "Zone 1[%s]" {channel="lupus:xt2:7b2410cf:state_a1_msg"}
String Lupus_State2_Msg "Zone 2[%s]" {channel="lupus:xt2:7b2410cf:state_a2_msg"}

#file *.sitemap
    Frame {
        Default item=Lupus_Mode1
        Default item=Lupus_Mode2
        Default item=Lupus_State1
        Default item=Lupus_State2
        Default item=Lupus_State1_Msg
        Default item=Lupus_State2_Msg
    }

#file lupus.map
0=Disarmed
1=Armed
2=Homemodus 1
3=Homemodus 2
4=Homemodus 3

#file lupus.rule

rule "Alarm Zone 1"
    when Item Lupus_State1 received update
then
    if (Lupus_State1.state != NULL && Lupus_State1.state>=0x130 && Lupus_State1.state<=0x136) { 
        postUpdate(Shutter_All, DOWN)
    }
end

	

## Any custom content here!
The icons lupus_shield are bundled with the org.openhab.binding.lupus.jar

