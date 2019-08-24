# ChatFilter v3.0 Specifications

# Introduction

This is the third version of the Chat Filter for Craftathon. It's the third version because the first one was poorly designed, slow, inefficient, had more false positives than lines of code (It had a *lot* of lines of code), and was brutal. The second version was better, still around 250ms, and poorly designed.

This is version 3 of the Chat Filter, which uses less than half the amount of code v2 used, and has an insane performance increase (Sometimes upwards of 10x), built upon a very solid plan beforehand, with all its features integrated from the beginning.

The original flowchart of the Chat Filter can be found [here](https://drive.google.com/file/d/1tqvZT-RA78aj1LBpQqk_jVMRbP4KrDth/view?usp=sharing). This flowchart was made before the development of v3 even started, and the internal process shown in the chart is nearly identical at most parts to how the inner code works. This document outlines some of the core concepts the Chat Filter v3 uses, and gives an understanding of how it detects certain things, how to use things, etc.

# Custom Utilities

## QChars

QChars are a custom created object to easily compare characters that have multiple values. Its name is short for **Quantum Characters**, as they can *be* multiple values at the same time, and be identified as the same values, from other characters, or QChars. This is used when characters like **A** and **4** look similar, but can not be compared with a single **.equals** method. A QChar can make an **A** and a **4** be the same value, in only one character space in a **QString** (See below). Instead of a word being as an example **ass**, which can not be compared with **4ss**, **a5s**, **a55**, etc., it may print out as **(0|a|4)(1|s|5)(2|s|5)**. This is compiled into a **QString** (See below) where each character can be compared to any of its values (Shown after the first character, an index, all separated by the pipe character **|**). A QChar can be created by a normal character to be compared with other QChars. A list of QChars are created in another class, each QChar consisting of every character that looks like one another, so any of the containing values/chars can be equal to each of those QChars, which each bad word is made up of those QChars in a **QString** (See below). A QChar can contain a repetition value, which is used for characters that are required to be repeated. If it is more than 1, this value is displayed before the index when printed out with an **x** before it, for clarification that it is the repetition value. As an example, **ass**, may be printed out as **(0|a|4)(x2|1|s|5)**.

## QStrings

QStrings, short for **Quantum Strings**, is just a collection of **QChar**s, that have many of the same methods as a normal String, because it behaves as one. QStrings can be compared to each other using **#equals**, **#equalsIgnoreCase**, **#contains**, etc. A QString can be created by a normal String to be compared with other QStrings.

# Word List

## Formatting

### Exact Strings

All words will automatically have repeating characters truncated, unless surrounded with curly brackets. Eg. **ass** will be truncated to **as** unless specified in the word list as being **a{ss}**, which will look for **ass** and not detect things like **as**, **45**, etc. This goes for any characters in the brackets, as characters in the brackets will never be shortened. An example of this is if a bad word is **a{sstt}**, it would stay to be **asstt** and would ***not*** be shortened down to **ast**. This is the equivalent of doing **a{ss}{tt}** and will match **asssttt**, **asstt**, **asssstttt**, but will ***not*** match with **assst**, **asttt**, **ast**, etc.

### No Separation

All words are allowed to be separated by as many white spaces as possible, which can cause false positives with some words. An example of this is having a bad word **sex**, and an input string of **… his extra …**. The word **s ex** is found inbetween those words, but no bad word is actually there. There are countless other examples of this, and they can mostly be solved with the **!** character. Putting a **!** in a bad word in between two characters insures that there is no space between the characters. So if the bad word instead of **sex** is **s!ex**, the input **… his extra …** is safe, because there is a space between the **s** and **ex**.

## Priorities

Priorities are a big factor in the Chat Filter, in which bad words labeled with priority 0 may be allowed in a word, but not start a word. An example is **ass**, which is  usually a priority 0, **grass** will not be detected, as the priority 0 word is contained in the whole word. However, words like **assistant** will be blocked without proper whitelisting, as a priority 0 word is starting a word.

Priority 1 words follow a very strict policy of they can not be contained in any way whatsoever in a word. An example of this is **fuck**, which is usually priority 1, some things it will detect as bad is **testfuck**, **fucktest**, **testfucktest**, etc.

## Whitelisting

Whitelisting is required to allow certain words, without the use of breakable filter rules. Whitelisted words are allowed and skipped over through the checking process after the first check of the word, even if it contains or even *is* a blocked word. An example of this is if **ass** is priority 1 (This happens with priority 0 as well) blocked word, and a user types in **… as she …** The system sees the first letters as  **as s** and decides it is a swear. If you have **as** a whitelisted word, it does not recognize it as bad, because once it sees **as** in the example, it goes on to the next word.

Whitelisted words are added via a list of 10,000 words (Found [here](https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english.txt)) which have any of the given bad words contained in them (But not being equal to). More words should be added in addition to this list, but it is sufficient for the time being.

Whitelisted words can be removed from the word list by appending a **-** before it. For example, the word list may have **livesex** (Don't ask me, it was on there) and whitelists it, so people could say that, because it contains **sex** in it. This is obviously not suppose to be whitelisted, so a whitelist value may be **-livesex**. All negated words from the whitelist using **-** are processed after the initial word list fetching and parsing, to ensure negations work properly.

## Number Filter

Some words can be detected as bad just by being numbers, or reaches a certain threshold percentage of being numbers. By default this threshold is 75%, which means a word will not be marked as bad if it is 75% or more numbers. An example of this is if you have a priority 0/1 word **ass**, because the number **4** can also be **A**, and the number **5** can also be **S**, the number **455** can be thought of by the system as **ass**. The filter's system determines if a word is 75% or more just numbers, if so, it is determined a safe word, if not and still 75% or more numbers, most likely unreadable without trying to read a bad word in it.

# How The System Works

In standard operation, the QStrings are initialized by a method, and then the whitelisted words are initialized with a separate method directly after. Those things only happen once at the beginning, and usually take the most processing power out of all the things ran in the system, but still is not very intensive. When an input word comes in, the system runs through each QString of the blocked words, and with that word adds to a list the offsets and lengths of blocked words from the input using one method.

When the input is ready to be parsed, it is stripped of repeating characters and converted into a QString. The system then loops through each bad word, and with each current word, it goes through each QChar of the input QString. If the current bad word starts with the current QChar, keep checking if the QChars in front of it are part of the bad word. If it reaches the end of the bad word, If it passes the whitelist and number threshold check, and the bad word is priority 0 and there is a space before it, or it is priority 1, add to internal bad word list, that is the same throughout all loops of bad words.

When each bad word has been looped through, sort the bad words from the internal list from least to greatest by their index they appear in. It then goes through each bad word, and checks if the previous and next word overlap the current word, or they overlap it. It removes the lowest priority word that is being overlapped/overlapping.

After that, the system goes through each of the characters of the input string, if it comes across an index of a bad word in the final list set above, replace it with a *.