package com.kejing.spatialarcade.domain.usecase

import com.kejing.spatialarcade.domain.model.Point
import kotlin.math.*

/** Display-only phrase magnification. Physics and MIDI never consume these values.
 * The marble and all note/rail groups share this exact uniform transform.
 * Launch and collection use the native base coordinates; transitions are continuous.
 */
class MusicPresentation {
    companion object { const val phraseScale=1.65f; const val bottom=.11f; const val top=.72f }
    var scale=1f; private set
    private val focus=Point(0f,.44f,0f)
    fun update(dt:Float,physics:MusicSimulation){
        val target=if(physics.started&&!physics.launching&&!physics.completed&&!physics.returning&&!physics.missed)phraseScale else 1f
        scale+=(target-scale)*(1f-exp(-dt.coerceIn(0f,.1f)*12f))
        if(abs(scale-target)<.0001f)scale=target
    }
    fun reset(){scale=1f}
    fun map(p:Point)=focus+(p+focus*-1f)*scale
    fun routeOffset(scroll:Float)=focus*(1f-scale)+Point(0f,scroll*scale,0f)
    fun visible(low:Float,high:Float,scroll:Float)=map(Point(0f,high+scroll,0f)).y>(if(scale<1.1f).052f else bottom)&&map(Point(0f,low+scroll,0f)).y<(if(scale<1.1f).77f else top)
}

/** Edge latch prevents one held press or an additional hand from toggling twice. */
class PressLatch {
    var down=false;private set
    fun press(pressed:Boolean):Boolean {val rising=pressed&&!down;down=pressed;return rising}
}
