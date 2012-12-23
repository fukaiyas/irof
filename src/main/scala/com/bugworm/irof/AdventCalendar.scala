package com.bugworm.irof

import AdventCalendar._
import javafx.application.Application
import javafx.scene.text.FontWeight
import scalafx.Includes._
import scalafx.animation.KeyFrame
import scalafx.animation.Timeline
import scalafx.beans.binding.NumberBinding.sfxNumberBinding2jfx
import scalafx.beans.property.DoubleProperty
import scalafx.event.ActionEvent
import scalafx.geometry.BoundingBox.sfxBoundingBox2jfx
import scalafx.geometry.BoundingBox
import scalafx.scene.canvas.Canvas
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.control.Button
import scalafx.scene.image.Image.sfxImage2jfx
import scalafx.scene.image.Image
import scalafx.scene.input.MouseEvent.sfxMouseEvent2jfx
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.StackPane.sfxStackPane2jfx
import scalafx.scene.layout.StackPane
import javafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.Font
import scalafx.scene.ImageCursor
import scalafx.scene.Scene
import scalafx.stage.Stage.sfxStage2jfx
import scalafx.stage.Stage
import scalafx.geometry.Pos
import scalafx.scene.layout.HBox
import scalafx.scene.layout.BorderPane
import scalafx.beans.property.BooleanProperty
import scalafx.scene.control.Label
import scalafx.scene.Group

object AdventCalendar {

    val backimg = new Image("/back.png")
    val balloonimg = new Image("/balloon.png")
    val irofimg = new Image("/irof.png")
    val cursorimg = new Image("/cursor.png")

    val screenWidth = 1000
    val screenHeight = 750
    val screen = Rectangle(0, 0, screenWidth, screenHeight)

    val irofX = screenWidth - irofimg.width.value
    val irofY = screenHeight - irofimg.height.value

    var count = 0
    val power = DoubleProperty(50)
    var balloons = List[Balloon]()
    var enemies = List[Enemy]()

    val mainloop = new Timeline{
        cycleCount = Timeline.INDEFINITE
    }

    val startbutton = new Label("[START]"){
        textFill = Color.BLACK
        style = "-fx-background-color: DarkGray"
        font = Font.font("monospaced", FontWeight.BOLD, 48)
    }
    val subscreen = new Group
    val statusscreen = new BorderPane{
        center = new Group(startbutton)
        bottom = new HBox{
            content = Seq(new Rectangle{
                fill = Color.LIGHTGREEN
                width <== power * 8
                height = 50
            },
            new Rectangle{
                fill = Color.RED
                width <== (- power + 100) * 8
                height = 50
            })
        }
    }

    def main(args : Array[String]) = Application.launch(classOf[AdventCalendar])
}

class AdventCalendar extends Application {

    var mouse : Option[MouseEvent] = None

    def start(stage: javafx.stage.Stage): Unit = {

        startbutton.onMouseClicked = {e : MouseEvent =>
            startbutton.visible = false
            count = 0
            Boss.reset()
            power.value = 10
            balloons = List[Balloon]()
            enemies = List[Enemy]()
            subscreen.children.clear()
            statusscreen.top = null
            mainloop.play
        }

        val canvas = new Canvas(screenWidth, screenHeight)
        new Stage(stage){
            title = "irof Advent Calendar 2012 - Save the Earth! irof-san"
            scene = new Scene(new StackPane{
	            onMousePressed = {e : MouseEvent => mouse = Option(e)}
	            onMouseDragged = {e : MouseEvent => mouse = Option(e)}
	            onMouseReleased = {mouse = None}
	            onMouseClicked = {e : MouseEvent => special(e)}
                content = Seq(canvas, statusscreen, subscreen)
                cursor = new ImageCursor(cursorimg, cursorimg.getWidth / 2, cursorimg.getHeight / 2)
            }, screenWidth, screenHeight)
        }.show

        canvas.graphicsContext2D.drawImage(backimg, 0, 0)
        mainloop.keyFrames = KeyFrame(30 ms, "[irof]", runFrame(canvas.graphicsContext2D))
    }

    def special(e : MouseEvent) : Unit = {
        
    }
    def runFrame(gc : GraphicsContext): Unit = {
        execute()
        draw(gc)
    }
    def execute(): Unit = {
        count += 1
        if(count == 2000){
            enemies = Boss :: enemies
        }
        balloons.foreach{ _.move() }
        mouse.foreach{e =>
            if(count % 3 == 0){
	            val mx =  e.getX - irofX - balloonimg.getWidth / 2
	            val my  = e.getY - irofY - balloonimg.getHeight / 2
	            val len = Math.sqrt(mx * mx + my * my)
	            balloons = new Balloon(irofX, irofY, mx * 16 / len, my * 16 / len, balloonimg) :: balloons
            }
        }
        if(Math.random > 0.8){
            enemies = new Meteo :: enemies
        }
        enemies.foreach{e =>
            e.move()
            val i = balloons.indexWhere{ b => b.active && b.bounds.intersects(e.bounds) }
            if(i >= 0){
            	balloons(i).active = false
            	e.hp -= 1
            	if(power.value < 100)power.value = power.value + 1
            }
            if(e.crash){
                power.value = power.value - 1
                e.hp = 0
                if(power.value < 0)power.value = 0
            }
            if(Boss.crash || power.value <= 0){
                gameover()
            }
            if(!Boss.active){
                congratulations()
            }
        }
        balloons = balloons.filter{ _.active }
        enemies = enemies.filter{ _.active }
    }
    def draw(gc : GraphicsContext): Unit = {
        gc.drawImage(backimg, 0, 0)
        enemies.foreach{ ci => gc.drawImage(ci.image, ci.x, ci.y) }
        balloons.foreach{ ci => gc.drawImage(ci.image, ci.x, ci.y) }
        gc.drawImage(irofimg, irofX, irofY)
    }

    def gameover(): Unit = {
        statusscreen.top = new Label("GAME OVER"){
            textFill = Color.RED
    		style = "-fx-background-color: DarkRed ; -fx-padding : 20"
            font = Font.font("monospaced", FontWeight.BOLD, 64)
            alignment = Pos.TOP_CENTER
        }
        mainloop.pause
        startbutton.visible = true
    }
    def congratulations(): Unit = {
        statusscreen.top = new Label("Congratulations!!"){
            textFill = Color.LIGHTGREEN
    		style = "-fx-background-color: DarkGreen ; -fx-padding : 20"
            font = Font.font("monospaced", FontWeight.BOLD, 64)
            alignment = Pos.TOP_CENTER
        }
        mainloop.pause
        startbutton.visible = true
    }
}

class Balloon(
        var x : Double,
        var y : Double,
        var vx : Double,
        var vy : Double,
        val image : Image){

    var active = true

    def bounds = new BoundingBox(x + 10, y + 10, image.width.value - 20, image.height.value - 20)
    def move() = {
        x += vx
        y += vy
    }
    def inside() : Boolean = {
        screen.intersects(bounds)
    }
}
