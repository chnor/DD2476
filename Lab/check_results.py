#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys, re

pattern = re.compile('^\d*\.$')
query = []
stopwords = ['i', 'och', 'den']
target = int(sys.argv[2])

iter = 0
for line in file(sys.argv[1]):
	iter += 1
	tokens = line.split()
	if len(tokens) < 1: continue
	
	if pattern.match(tokens[0]):
		if iter != target: continue
		for line in file(tokens[1]):
			line = line.strip()
			# words = re.split('[^a-zA-Z0-9åÅÄÖåäöéàü]', line)
			words = re.split('[ \|\<\>\-!\"#%&\/\(\)\=\?\{\[\]\}\*\',\.:;]', line)
			words = [w for w in words if w != ""]
			print ' '.join(words[0:100])
			print ''
			
			words = ['']*10 + words + ['']*10
			
			words_lists = [words[i:i+18] for i in range(0, len(words))]
			for context in words_lists:
				if len(context) < 9: break
				center_word = context[8]
				# center_word = center_word.strip(delimiters)
				# print center_word
				if center_word.lower() in query:
					context = [w.lower() in query and w or '\033[90m' + w + '\033[0m' for w in context]
					print ' '.join(context)
	else:
		query = line.strip().split()
		query = [x.lower() for x in query if not x in stopwords]