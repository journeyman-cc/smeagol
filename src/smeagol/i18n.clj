(ns ^{:doc "Internationalisation."
      :author "Simon Brooke"}
  smeagol.i18n
  (:require [noir.session :as session]
            [noir.io :as io]
            [smeagol.authenticate :as auth]
            [smeagol.formatting :refer [md->html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;
;;;; Smeagol: a very simple Wiki engine.
;;;;
;;;; This program is free software; you can redistribute it and/or
;;;; modify it under the terms of the GNU General Public License
;;;; as published by the Free Software Foundation; either version 2
;;;; of the License, or (at your option) any later version.
;;;;
;;;; This program is distributed in the hope that it will be useful,
;;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;;; GNU General Public License for more details.
;;;;
;;;; You should have received a copy of the GNU General Public License
;;;; along with this program; if not, write to the Free Software
;;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
;;;; USA.
;;;;
;;;; Copyright (C) 2014 Simon Brooke
;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def i18n-cache {})


(defn parse-accept-language
  "The `Accept-Language` header is defined in section 14.4 of RFC 2616. It is composed
  of a sequence of 'language tags' defined in section 3.10 of the same document.
  Generally a language tag is a short alpha string (the 'primary language tag'),
  optionally followed by a hyphen and another short alpha string (a 'sub tag').
  A sequence of more than one sub-tag is allowed.

  Generally a two-character primary tag will be an ISO-639 language code and any
  initial two-character sub tag will be an ISO-3166 country code.

  Each language tag may optionally followed by a semi-colon followed by a 'q' value,
  specified `q=0.8` where the numeric value is a real number in the range 0...1
  and represents the user's preference for this language (higher is better). If
  no q value is supplied 1 is assumed.

  Language specifiers are separated by a comma followed by a space."
  [accept-language-header]
  )
