// =====================
// Create required vars
// =====================
var output = $('.output');
var input = $('textarea.input');
var toOutput;

// Creates the event listner for the comands ==
// Yes this is a long one - could do with some
// improvements ===============================
input.keydown(function(e) {
	if (e.keyCode  == 13 && !event.ctrlKey) {
		var inputVal = $.trim(input.val());
        event.preventDefault();
        input.val('');

		if (inputVal == "help") {
			help();
		} else if (inputVal == "ping") {
			pong();
		} else if (inputVal == "about") {
			aboutMe();
		} else if (inputVal == "contact") {
			contactMe();
		} else if (inputVal == "clear") {
			clearConsole();
		} else if (inputVal.startsWith("say") === true) {
			sayThis(inputVal);
		} else if (inputVal.startsWith("sudo") === true) {
			sudo(inputVal);
		} else if (inputVal == "time") {
			getTime();
		} else if (inputVal == 'whats that sound' || inputVal == 'what\'s that sound' || inputVal == 'whats that sound?') {
			seperator();
			Output('<span class="blue">' + inputVal + '</span></br><span class="red">Machine Broken!</span></br>');
			seperator();
		} else if (inputVal.startsWith("exit") === true) {
			Output('<span class="blue">Goodbye! Comeback soon.</span>');
			setTimeout(function() {
				window.open('https://codepen.io/MarioDesigns');
			}, 1000);
		} else {
			Output('<span>command not found</span></br>');
		}
	}
    else if (event.keyCode == 13 && event.ctrlKey) {
        var content = this.value;
        var caret = getCaret(this);
        this.value = content.substring(0,caret)+"\n"+content.substring(caret,content.length);
        event.stopPropagation();
    }
});

function getCaret(el) {
  if (el.selectionStart) {
    return el.selectionStart;
  } else if (document.selection) {
    el.focus();

    var r = document.selection.createRange();
    if (r == null) {
      return 0;
    }

    var re = el.createTextRange(),
        rc = re.duplicate();
    re.moveToBookmark(r.getBookmark());
    rc.setEndPoint('EndToStart', re);

    return rc.text.length;
  }
  return 0;
}
// functions related to the commands typed
// =======================================

//clears the screen
function clearConsole() {
	output.html("");
	Output('<span>clear</span></br>');
}

// prints out a list of "all" comands available
function help() {
	var commandsArray = ['Help: List of available commands', '>help', '>about', '>contact', '>ping', '>time', '>clear', '>say'];
	for (var i = 0; i < commandsArray.length; i++) {
		var out = '<span>' + commandsArray[i] + '</span><br/>'
		Output(out);
	}
}

// prints the result for the pong command
function pong() {
	Output('<span>pong</span></br><span class="pong"><b class="left">|</b><b class="right">|</b></span></br>');
}

// function to the say command
function sayThis(data) {
	data = data.substr(data.indexOf(' ') + 1);
	Output('<span class="green">[say]:</span><span>' + data + '</span></br>');
}

// sudo?!? not really
function sudo(data) {
	data = data.substr(data.indexOf(' ') + 1);
	if (data.startsWith("say") === true) {
		data = "Not gona " + data + " to you, you don\'t own me!"
	} else if (data.startsWith("apt-get") === true) {
		data = "<span class='green'>Updating...</span> The cake is a lie! There is nothing to update..."
	} else {
		data = "The force is week within you, my master you not be!"
	}
	Output('<span>' + data + '</span></br>');
}

// function to get current time...not
function getTime() {
	Output('<span>It\'s the 21st century man! Get a SmartWatch</span></br>');
}

function aboutMe() {
	var aboutMeArray = ['>About:', 'Hi There!', 'I\'m Mario, a Digital Developer working [@wearecollider](http://www.wearecollider.com) during the day and a designer, freerider, pcbuilder, droneracer and science lover on my free time.', 'Fell free to follow me on twitter @MDesignsuk - see contact page.'];
	seperator();
	for (var i = 0; i < aboutMeArray.length; i++) {
		var out = '<span>' + aboutMeArray[i] + '</span><br/>'
		Output(out);
	}
	seperator();
}

function contactMe() {
	var contactArray = ['>Contact:', '[GitHub](https://github.com/Mario-Duarte)', '[BitBucket](https://bitbucket.org/Mario_Duarte/)', '[CodePen](https://codepen.io/MarioDesigns/)', '[Twitter](https://twitter.com/MDesignsuk)'];
	seperator();
	for (var i = 0; i < contactArray.length; i++) {
		var out = '<span>' + contactArray[i] + '</span><br/>'
		Output(out);
	}
	seperator();
}

// Prints out the result of the command into the output div
function Output(data) {
	$(data).appendTo(output);
}