package com.bugworm.irof

import scalafx.Includes._
import scalafx.scene.image.Image
import com.bugworm.irof.Enemy._
import AdventCalendar._
import scalafx.geometry.BoundingBox

object Enemy{
    val meteoimg = new Image("/meteo.png")
    val bossimg = new Image("/boss.png")
}

abstract class Enemy{
    var x : Double
    var y : Double
    var hp = 1
    val image : Image
    def bounds = new BoundingBox(x + 20, y + 20, image.width.value - 40, image.height.value - 40)
    def move() : Unit
    def crash() : Boolean = {
        return x > screenWidth - 200 && y > screenHeight - 200
    }
    def active() : Boolean = {hp > 0}
}

class Meteo extends Enemy{
    val image = meteoimg
    val top = Math.random > 0.5
    var x = if(top) Math.random * 800 - 100 else -150
    var y = if(top) -150 else Math.random * 600 - 100
    val dx = irofX - x
    val dy = irofY - y
    val dis = Math.sqrt(dx * dx + dy * dy)
    val vx = 10 * dx / dis
    val vy = 10 * dy / dis

    override def move() : Unit = {
        x += vx
        y += vy
    }
}

object Boss extends Enemy{
    val image = bossimg
    var x = -500d
    var y = -500d
    val dx = irofX - x
    val dy = irofY - y
    val dis = Math.sqrt(dx * dx + dy * dy)
    val vx = 5 * dx / dis
    val vy = 5 * dy / dis

    override def move() : Unit = {
        x += vx
        y += vy
    }
    override def crash() : Boolean = {
        return x > screenWidth - 500 && y > screenHeight - 500
    }
    def reset() : Unit = {
        hp = 50
        x = -500d
        y = -500d
    }
}