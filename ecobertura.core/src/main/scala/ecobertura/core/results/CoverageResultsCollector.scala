package ecobertura.core.results

import java.util.logging.Logger

import net.sourceforge.cobertura.coveragedata.ProjectData

import org.eclipse.debug.core._
import org.eclipse.debug.core.model.IProcess

import ecobertura.core.cobertura.CoberturaWrapper
import ecobertura.core.data.CoverageSession

object CoverageResultsCollector {
	def collect = new CoverageResultsCollector
}

class CoverageResultsCollector extends IDebugEventSetListener {
	val logger = Logger getLogger "ecobertura.core.results"

	private var listeners = List[CoverageResultsListener]() 
	private var currentLaunch: Option[ILaunch] = None
	
	DebugPlugin.getDefault addDebugEventListener this
	
	override def handleDebugEvents(events: Array[DebugEvent]) = {
		def isCoverageLaunchTerminationEvent(event: DebugEvent) : Boolean = {
			def isLaunchTerminationEvent(event: DebugEvent) : Boolean = 
				event.getSource.isInstanceOf[IProcess] && event.getKind == DebugEvent.TERMINATE

			if (!isLaunchTerminationEvent(event)) false
				
			val launch = event.getSource.asInstanceOf[IProcess].getLaunch
			Some(launch) == currentLaunch
		}
		
		def notifyListeners(coverageSession: CoverageSession) = 
			listeners foreach (_.coverageRunCompleted(coverageSession))

		def retrieveCoverageData = 
			CoverageSession.fromCoberturaProjectData(CoberturaWrapper.get.projectDataFromDefaultFile)

		for (event <- events if isCoverageLaunchTerminationEvent(event)) {
			logger fine "detected termination of covered launch"
			notifyListeners(retrieveCoverageData)
		}
	}
	
	def coveredLaunchStarted(launch: ILaunch) = {
		assert(launch != null)
		currentLaunch = Some(launch)
	}
	
	def addListener(listener: CoverageResultsListener) = listeners ::= listener
	def removeListener(listener: CoverageResultsListener) = listeners.filterNot(_.equals(listener)) 
}