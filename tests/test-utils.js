'use strict';

/***
 * Exported functions to be used in the testing scripts.
 */
module.exports = {
  uploadImageBody,
  genNewUser,
  genNewUserReply,
  selectUser,
  selectUserSkewed,
  genNewAuction,
  genNewBid,
  genNewQuestion,
  genNewQuestionReply,
  decideToCoverBid,
  decideToReply,
  decideNextAction,
  random80,
  random50,
}


const Faker = require('faker/locale/en_US')
const fs = require('fs')
const path = require('path')

var imagesIds = []
var images = []
var users = []

// Auxiliary function to select an element from an array
Array.prototype.sample = function(){
	   return this[Math.floor(Math.random()*this.length)]
}

// Auxiliary function to select an element from an array
Array.prototype.sampleSkewed = function(){
	return this[randomSkewed(this.length)]
}

// Returns a random value, from 0 to val
function random( val){
	return Math.floor(Math.random() * val)
}

// Returns the user with the given id
function findUser( id){
	for( var u of users) {
		if( u.id === id)
			return u;
	}
	return null
}

// Returns a random value, from 0 to val
function randomSkewed( val){
	let beta = Math.pow(Math.sin(Math.random()*Math.PI/2),2)
	let beta_left = (beta < 0.5) ? 2*beta : 2*(1-beta);
	return Math.floor(beta_left * val)
}


// Loads data about images from disk
function loadData() {
	var basedir
	if( fs.existsSync( '/images')) 
		basedir = '/images'
	else
		basedir =  'images'	
	fs.readdirSync(basedir).forEach( file => {
		if( path.extname(file) === ".jpeg") {
			var img  = fs.readFileSync(basedir + "/" + file)
			images.push( img)
		}
	})
	var str;
	if( fs.existsSync('users.data')) {
		str = fs.readFileSync('users.data','utf8')
		users = JSON.parse(str)
	} 
}

loadData();

/**
 * Sets the body to an image, when using images.
 */
function uploadImageBody(requestParams, context, ee, next) {
	requestParams.body = images.sample()
	return next()
}

/**
 * Process reply of the download of an image. 
 * Update the next image to read.
 */
function processUploadReply(requestParams, response, context, ee, next) {
	if( typeof response.body !== 'undefined' && response.body.length > 0) {
		imagesIds.push(response.body)
	}
    return next()
}

/**
 * Select an image to download.
 */
function selectImageToDownload(context, events, done) {
	if( imagesIds.length > 0) {
		context.vars.imageId = imagesIds.sample()
	} else {
		delete context.vars.imageId
	}
	return done()
}


/**
 * Generate data for a new user using Faker
 */
function genNewUser(context, events, done) {
	const first = `${Faker.name.firstName()}`
	const last = `${Faker.name.lastName()}`
	context.vars.id = first + "." + last
	context.vars.name = first + " " + last
	context.vars.pwd = `${Faker.internet.password()}`
	return done()
}


/**
 * Process reply for of new users to store the id on file
 */
function genNewUserReply(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300 && response.body.length > 0)  {
		let u = JSON.parse( response.body)
		users.push(u)
		fs.writeFileSync('users.data', JSON.stringify(users));
	}
    return next()
}

/**
 * Select user
 */
function selectUser(context, events, done) {
	if( users.length > 0) {
		let user = users.sample()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}


/**
 * Select user
 */
function selectUserSkewed(context, events, done) {
	if( users.length > 0) {
		let user = users.sampleSkewed()
		context.vars.user = user.id
		context.vars.pwd = user.pwd
	} else {
		delete context.vars.user
		delete context.vars.pwd
	}
	return done()
}

/**
 * Generate data for a new channel
 * Besides the variables for the auction, initializes the following vars:
 * numBids - number of bids to create, if batch creating 
 * numQuestions - number of questions to create, if batch creating 
 * bidValue - price for the next bid
 */
function genNewAuction(context, events, done) {
	context.vars.title = `${Faker.commerce.productName()}`
	context.vars.description = `${Faker.commerce.productDescription()}`
	context.vars.minimumPrice = `${Faker.commerce.price()}`
	context.vars.bidValue = context.vars.minimumPrice + random(3)
	var maxBids = 5
	if( typeof context.vars.maxBids !== 'undefined')
		maxBids = context.vars.maxBids;
	var maxQuestions = 2
	if( typeof context.vars.maxQuestions !== 'undefined')
		maxQuestions = context.vars.maxQuestions;
	var d = new Date();
	d.setTime(Date.now() + random( 300000));
	context.vars.endTime = d.toISOString();
	if( Math.random() > 0.2) { 
		context.vars.status = "OPEN";
		context.vars.numBids = random( maxBids);
		context.vars.numQuestions = random( maxQuestions);
	} else {
		context.vars.status = "CLOSED";
		delete context.vars.numBids;
		delete context.vars.numQuestions;
	}
	return done()
}

/**
 * Generate data for a new bid
 */
function genNewBid(context, events, done) {
	if( typeof context.vars.bidValue == 'undefined') {
		if( typeof context.vars.minimumPrice == 'undefined') {
			context.vars.bidValue = random(100)
		} else {
			context.vars.bidValue = context.vars.minimumPrice + random(3)
		}
	}
	context.vars.value = context.vars.bidValue;
	context.vars.bidValue = context.vars.bidValue + 1 + random(3)
	return done()
}

/**
 * Generate data for a new question
 */
function genNewQuestion(context, events, done) {
	context.vars.text = `${Faker.lorem.paragraph()}`;
	return done()
}

/**
 * Generate data for a new reply
 */
function genNewQuestionReply(context, events, done) {
	delete context.vars.reply;
	if( Math.random() > 0.5) {
		if( typeof context.vars.auctionUser !== 'undefined') {
			var user = findUser( context.vars.auctionUser);
			if( user != null) {
				context.vars.auctionUserPwd = user.pwd;
				context.vars.reply = `${Faker.lorem.paragraph()}`;
			}
		}
	} 
	return done()
}


/**
 * Decide whether to bid on auction or not
 * assuming: user context.vars.user; bids context.vars.bidsLst
 */
function decideToCoverBid(context, events, done) {
	delete context.vars.value;
	if( typeof context.vars.user !== 'undefined' && typeof context.vars.bidsLst !== 'undefined' && 
			context.vars.bidsLst.constructor == Array && context.vars.bidsLst.length > 0) {
		let bid = context.vars.bidsLst[0];
		if( bid.user !== context.vars.user && Math.random() > 0.5) {
			context.vars.value = bid.value + random(3);
			context.vars.auctionId = bid.auctionId;
		}
	}
	return done()
}

/**
 * Decide whether to reply
 * assuming: user context.vars.user; question context.vars.questionOne
 */
function decideToReply(context, events, done) {
	delete context.vars.reply;
	if( typeof context.vars.user !== 'undefined' && typeof context.vars.questionOne !== 'undefined' && 
			context.vars.questionOne.user === context.vars.user && 
			typeof context.vars.questionOne.reply !== String &&
			Math.random() > 0) {
		context.vars.reply = `${Faker.lorem.paragraph()}`;
	}
	return done()
}


/**
 * Decide next action
 * 0 -> browse popular
 * 1 -> browse recent
 */
function decideNextAction(context, events, done) {
	delete context.vars.auctionId;
	let rnd = Math.random()
	if( rnd < 0.075)
		context.vars.nextAction = 0; // browsing recent
	else if( rnd < 0.15)
		context.vars.nextAction = 1; // browsing popular
	else if( rnd < 0.225)
		context.vars.nextAction = 2; // browsing user
	else if( rnd < 0.3)
		context.vars.nextAction = 3; // create an auction
	else if( rnd < 0.8)
		context.vars.nextAction = 4; // checking auction
	else if( rnd < 0.95)
		context.vars.nextAction = 5; // do a bid
	else
		context.vars.nextAction = 6; // post a message
	if( context.vars.nextAction == 2) {
		if( Math.random() < 0.5)
			context.vars.user2 = context.vars.user
		else {
			let user = users.sample()
			context.vars.user2 = user.id
		}
	}
	if( context.vars.nextAction == 3) {
		context.vars.title = `${Faker.commerce.productName()}`
		context.vars.description = `${Faker.commerce.productDescription()}`
		context.vars.minimumPrice = `${Faker.commerce.price()}`
		context.vars.bidValue = context.vars.minimumPrice + random(3)
		var d = new Date();
		d.setTime(Date.now() + 60000 + random( 300000));
		context.vars.endTime = d.toISOString();
		context.vars.status = "OPEN";
	}
	if( context.vars.nextAction >= 4) {
		let r = random(3)
		var auct = null
		if( r == 2 && typeof context.vars.auctionsLst == 'undefined')
			r = 1;
		if( r == 2)
  			auct = context.vars.auctionsLst.sample();
		else if( r == 1)
  			auct = context.vars.recentLst.sample();
		else if( r == 0)
  			auct = context.vars.popularLst.sample();
		if( auct == null) {
			return decideNextAction(context,events,done);
		}
		context.vars.auctionId = auct.id
		context.vars.imageId = auct.imageId
	}
	if( context.vars.nextAction == 5)
		context.vars.text = `${Faker.lorem.paragraph()}`;

	return done()
}


/**
 * Return true with probability 50% 
 */
function random50(context, next) {
  const continueLooping = Math.random() < 0.5
  return next(continueLooping);
}

/**
 * Return true with probability 50% 
 */
function random80(context, next) {
  const continueLooping = Math.random() < 0.8
  return next(continueLooping);
}

/**
 * Process reply for of new users to store the id on file
 */
function extractCookie(requestParams, response, context, ee, next) {
	if( response.statusCode >= 200 && response.statusCode < 300)  {
		for( let header of response.rawHeaders) {
			if( header.startsWith("scc:session")) {
				context.vars.mycookie = header.split(';')[0];
			}
		}
	}
    return next()
}


