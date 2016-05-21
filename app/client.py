#!/usr/bin/env python2
# coding=utf-8

import argparse
import logging
import os
import requests
import sys
import urlparse

s = requests.Session()
s.headers.update({'Accept': 'application/json,*/*'})


def upload(args):
    if args.verbose:
        print("Ordering download %s on %s" % (args.url, args.server))
    files_api_url = urlparse.urljoin(args.server, 'files')
    order = s.post(files_api_url, json={'urlToDownload': args.url})
    if args.show_http:
        print(order.text)
    if order.status_code != 202:
        print >> sys.stderr, 'Fail! %d' % order.status_code
        print >> sys.stderr, order.text
        sys.exit(2)
    location = order.headers['Location']
    if args.verbose:
        print('File will be available at: %s' % location)
    print(os.path.basename(urlparse.urlparse(location).path))


def download(args):
    if args.verbose:
        print("Downloading %s from %s" % (args.id, args.server))
    files_api_url = urlparse.urljoin(args.server, 'files/' + args.id)
    file_request = s.get(files_api_url, stream=True)
    if file_request.status_code != 200:
        print >> sys.stderr, 'Fail! %d' % file_request.status_code
        print >> sys.stderr, file_request.text
        sys.exit(2)

    file = sys.stdout if args.destination == '-' else open(args.destination, 'wb')
    for chunk in file_request.iter_content(chunk_size=1024):
        if chunk:  # filter out keep-alive new chunks
            file.write(chunk)
    file.close()


parser = argparse.ArgumentParser(prog='client')
parser.add_argument('--server', dest='server', default="http://localhost:8080/api/", help='Adres serwera warstwy zewnętrznej')
parser.add_argument('--verbose', dest='verbose', action='store_true')
parser.add_argument('--show-http', dest='show_http', action='store_true')
subparsers = parser.add_subparsers(help='sub-command help')

parser_upload = subparsers.add_parser('upload', help='Wgranie pliku do systemu')
parser_upload.add_argument('url', help='URL wskazujący plik do pobrania')
parser_upload.set_defaults(func=upload)

parser_download = subparsers.add_parser('download', help='Pobranie zawartości pliku')
parser_download.add_argument('id', help='Identyfikator pliku do pobrania')
parser_download.add_argument('destination', nargs='?', default='-', help='Nazwa docelowego pliku')
parser_download.set_defaults(func=download)

arguments = parser.parse_args()

if arguments.show_http:
    logging.basicConfig()
    logging.getLogger().setLevel(logging.DEBUG)
    requests_log = logging.getLogger("requests.packages.urllib3")
    requests_log.setLevel(logging.DEBUG)
    requests_log.propagate = True

arguments.func(arguments)
