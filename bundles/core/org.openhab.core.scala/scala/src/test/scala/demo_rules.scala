import org.openhab.core.scala.RuleSetFactory
import hammurabi.Rule
import hammurabi.Rule._
import org.openhab.core.items.Item
import org.openhab.core.scala.model.BusEvent._
import org.openhab.core.types.Command
import org.openhab.core.library.types.OnOffType
import org.openhab.core.library.types.OnOffType._
import org.openhab.core.library.items.NumberItem
import org.openhab.core.library.types.DecimalType
import org.openhab.core.library.items.SwitchItem
import scala.actors.Logger
import hammurabi.util.Logger
import org.openhab.core.scala.model._
import org.openhab.core.scala.model.SystemEventType._
import org.openhab.core.library.types.IncreaseDecreaseType._
import org.openhab.core.library.types.PercentType
import org.openhab.io.multimedia.actions.Audio
import org.openhab.io.multimedia.actions.Audio._
import org.joda.time.DateTime._
import org.openhab.core.persistence.extensions.PersistenceExtensions._
import org.openhab.model.script.actions.LogAction._

object ExampleRuleSetFactoryImpl extends RuleSetFactory {
  var timer = null
  
  def dimm(item: Item, cmd: Command) = {
    var percent = 0
    if(item.getState().isInstanceOf[DecimalType]) {
      percent = item.getState().asInstanceOf[DecimalType].intValue()
    }
        
    percent = percent + (cmd match {
      case INCREASE => +5
      case DECREASE => -5
    })
		
	if(percent<0)   percent = 0
	if(percent>100) percent = 100
	
	updated(item).to(new DecimalType(percent))
  } 
  
  override def generateRuleSet(): Set[Rule] = {
    Set(
      rule("System State") let {
        val e = any(kindOf[SystemEvent])
        
        then {
        	e.eventType match {          
        	case Startup => say("Welcome at openHab!")
        	case Shutdown => say("Good bye!")
        	}
        }
      },
      rule("Dimmed Light1") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "DimmedLight1")
        then {
        	dimm(e.item, e.cmd)
        }
      },
      rule("Dimmed Light2") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "DimmedLight2")
        then {
        	dimm(e.item, e.cmd)
        }
      },
      rule("Select Radio Station") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "Radio_Station")
        val cmd = e.cmd
        when (cmd.isInstanceOf[DecimalType])
        then {
	        val cmdVal = cmd.asInstanceOf[DecimalType].intValue()
	        cmdVal match {
	          case 0 => playStream(null)
				case 1 => playStream("http://metafiles.gl-systemhaus.de/hr/hr3_2.m3u")
				case 2 => playStream("http://mp3-live.swr3.de/swr3_m.m3u")
				case 3 => playStream("http://edge.live.mp3.mdn.newmedia.nacamar.net/radioffh/livestream.mp3.m3u")
	        }
        }
      },
      rule("Volume control") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "Volume")
        val cmd = e.cmd
        then {
          if (cmd.isInstanceOf[PercentType]) {
	        setMasterVolume(cmd.asInstanceOf[PercentType])
          }        
          else {
          cmd match {
		      case INCREASE => increaseMasterVolume(20)
		      case DECREASE => decreaseMasterVolume(20)	
		    }
        }
        updated(e.item).to(new DecimalType(getMasterVolume() * 100))
        }
      },
      rule("Say temperature on update") let {
        val e = kindOf[UpdateEvent] having (_.item.getName() == "Weather_Temperature")
        val state = e.state
        then {
          say("The temperature outside is " + state.format("%d") + " degrees celsius")
        }
      },
      rule("Update max and min temperatures(1)") let {
        val e1 = kindOf[StateEvent] having (_.item.getName() == "Weather_Temperature")
        val e2 = any(kindOf[SystemEvent])
        then {
          val item = kindOf[NumberItem] having (_.getName() == "Weather_Temperature")
          val max = kindOf[NumberItem] having (_.getName() == "Weather_Temp_Max")
          val min = kindOf[NumberItem] having (_.getName() == "Weather_Temp_Min")
    
          updated(max) to maximumSince(item, now).getState()
          updated(min) to minimumSince(item, now).getState()
        }
      },
      rule("persistence demo 2") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "DemoSwitch")
        when {
          !changedSince(e.item, now.minusSeconds(5))          
        }
        then {
        	logInfo("Persistence Demo", "You did not press this button during the last 5 seconds!")
        }
      },
      rule("Timer Demo") let {
        val e = kindOf[CommandEvent] having (_.item.getName() == "Light_GF_Corridor_Ceiling")
        then {          
          e.cmd match {
            case ON =>
              if(timer==null) {
            	  // first ON command, so create a timer to turn the light off again
            	  timer = createTimer(now.plusSeconds(10)) {
            		  sendCommand(Light_GF_Corridor_Ceiling, OFF)
            	  }
              } else {
            	  // 	subsequent ON command, so reschedule the existing timer
            	  timer.reschedule(now.plusSeconds(10))
              }
            case OFF =>
              // remove any previously scheduled timer
            	if(timer!=null) {
            		timer.cancel
            		timer = null
            	}
          }
        }
      }
      )
   }
}